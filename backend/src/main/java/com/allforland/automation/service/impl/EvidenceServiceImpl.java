package com.allforland.automation.service.impl;

import com.allforland.automation.client.AiEngineClient;
import com.allforland.automation.client.AiEngineClient.EvidenceFileCandidate;
import com.allforland.automation.client.AiEngineClient.EvidenceItemMatch;
import com.allforland.automation.client.AiEngineClient.RequiredItemSuggestion;
import com.allforland.automation.domain.CompanyFile;
import com.allforland.automation.domain.FileCategory;
import com.allforland.automation.domain.RequiredItem;
import com.allforland.automation.domain.RequirementSet;
import com.allforland.automation.domain.UploadedFile;
import com.allforland.automation.domain.User;
import com.allforland.automation.domain.ZipExport;
import com.allforland.automation.dto.EvidenceAnalysisResponse;
import com.allforland.automation.dto.RequiredItemResponse;
import com.allforland.automation.repository.CompanyFileRepository;
import com.allforland.automation.repository.RequiredItemRepository;
import com.allforland.automation.repository.RequirementSetRepository;
import com.allforland.automation.repository.ZipExportRepository;
import com.allforland.automation.service.EvidenceService;
import com.allforland.automation.service.FileStorageService;
import com.allforland.automation.service.UploadedFileService;
import com.allforland.automation.service.UserService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvidenceServiceImpl implements EvidenceService {

	private static final Logger log = LoggerFactory.getLogger(EvidenceServiceImpl.class);
	private static final int EVIDENCE_TEXT_MAX_CHARS = 800;

	private final RequirementSetRepository requirementSetRepository;
	private final RequiredItemRepository requiredItemRepository;
	private final ZipExportRepository zipExportRepository;
	private final CompanyFileRepository companyFileRepository;
	private final UploadedFileService uploadedFileService;
	private final UserService userService;
	private final FileStorageService fileStorageService;
	private final AiEngineClient aiEngineClient;

	public EvidenceServiceImpl(
			RequirementSetRepository requirementSetRepository,
			RequiredItemRepository requiredItemRepository,
			ZipExportRepository zipExportRepository,
			CompanyFileRepository companyFileRepository,
			UploadedFileService uploadedFileService,
			UserService userService,
			FileStorageService fileStorageService,
			AiEngineClient aiEngineClient) {
		this.requirementSetRepository = requirementSetRepository;
		this.requiredItemRepository = requiredItemRepository;
		this.zipExportRepository = zipExportRepository;
		this.companyFileRepository = companyFileRepository;
		this.uploadedFileService = uploadedFileService;
		this.userService = userService;
		this.fileStorageService = fileStorageService;
		this.aiEngineClient = aiEngineClient;
	}

	@Override
	@Transactional
	public EvidenceAnalysisResponse analyze(Long uploadedFileId, Long userId) {
		User user = userService.getById(userId);
		UploadedFile requirementDoc = uploadedFileService.getById(uploadedFileId);

		RequirementSet requirementSet = requirementSetRepository.save(new RequirementSet(user, requirementDoc));

		if (requirementDoc.getExtractedText() == null) {
			requirementSet.markFailed();
			return new EvidenceAnalysisResponse(requirementSet.getId(), requirementSet.getStatus(), List.of(), null, 0, 0);
		}

		List<RequiredItemSuggestion> suggestions = aiEngineClient.extractRequirements(requirementDoc.getExtractedText());
		if (suggestions.isEmpty()) {
			requirementSet.markFailed();
			return new EvidenceAnalysisResponse(requirementSet.getId(), requirementSet.getStatus(), List.of(), null, 0, 0);
		}

		List<CompanyFile> evidenceFiles = companyFileRepository
				.findAllByCategoryOrderByUploadedAtDesc(FileCategory.EVIDENCE)
				.stream()
				.filter(file -> file.getExtractedText() != null)
				.toList();

		List<EvidenceItemMatch> matches = aiEngineClient.matchEvidenceItems(
				suggestions,
				evidenceFiles.stream()
						.map(file -> new EvidenceFileCandidate(file.getId(), file.getFileName(), truncate(file.getExtractedText())))
						.toList());

		Map<Long, CompanyFile> evidenceById = new HashMap<>();
		for (CompanyFile file : evidenceFiles) {
			evidenceById.put(file.getId(), file);
		}

		List<RequiredItem> items = new ArrayList<>();
		for (int i = 0; i < suggestions.size(); i++) {
			RequiredItemSuggestion suggestion = suggestions.get(i);
			RequiredItem item = new RequiredItem(requirementSet, suggestion.name(), suggestion.description());

			EvidenceItemMatch match = i < matches.size() ? matches.get(i) : null;
			CompanyFile matchedFile = match != null && match.evidenceFileId() != null
					? evidenceById.get(match.evidenceFileId())
					: null;
			if (matchedFile != null) {
				item.applyMatch(matchedFile, match.reason());
			} else if (match != null) {
				item.markUnmatched(match.reason());
			}

			items.add(requiredItemRepository.save(item));
		}

		ZipExport zipExport = buildZipExport(requirementSet, items);
		requirementSet.markCompleted();

		int matchedCount = (int) items.stream().filter(RequiredItem::isMatched).count();
		int missingCount = items.size() - matchedCount;

		return new EvidenceAnalysisResponse(
				requirementSet.getId(),
				requirementSet.getStatus(),
				items.stream().map(RequiredItemResponse::from).toList(),
				zipExport != null ? zipExport.getId() : null,
				matchedCount,
				missingCount);
	}

	private String truncate(String text) {
		if (text == null) {
			return "";
		}
		return text.length() > EVIDENCE_TEXT_MAX_CHARS ? text.substring(0, EVIDENCE_TEXT_MAX_CHARS) : text;
	}

	private ZipExport buildZipExport(RequirementSet requirementSet, List<RequiredItem> items) {
		List<CompanyFile> matchedFiles = items.stream()
				.filter(RequiredItem::isMatched)
				.map(RequiredItem::getMatchedCompanyFile)
				.toList();
		if (matchedFiles.isEmpty()) {
			return null;
		}

		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				ZipOutputStream zip = new ZipOutputStream(buffer)) {
			// Matching already dedupes one-file-per-item, but two distinct CompanyFile rows could
			// still share the exact same file_name — guard the zip entry names too so that case
			// can never silently kill the whole export again (it did, via a duplicate-entry
			// IOException that a bare try/catch swallowed without a trace).
			Set<String> usedNames = new HashSet<>();
			for (CompanyFile file : matchedFiles) {
				zip.putNextEntry(new ZipEntry(uniqueEntryName(file.getFileName(), usedNames)));
				zip.write(fileStorageService.load(file.getStorageKey()));
				zip.closeEntry();
			}
			zip.finish();

			String storageKey = fileStorageService.store(buffer.toByteArray(), "evidence.zip");
			int missingCount = items.size() - matchedFiles.size();
			return zipExportRepository.save(new ZipExport(requirementSet, storageKey, matchedFiles.size(), missingCount));
		} catch (IOException e) {
			log.error("증빙자료 ZIP 생성 실패 (requirementSetId={})", requirementSet.getId(), e);
			return null;
		}
	}

	private String uniqueEntryName(String fileName, Set<String> usedNames) {
		if (usedNames.add(fileName)) {
			return fileName;
		}
		String base = fileName;
		String extension = "";
		int dot = fileName.lastIndexOf('.');
		if (dot > 0) {
			base = fileName.substring(0, dot);
			extension = fileName.substring(dot);
		}
		String candidate;
		int counter = 2;
		do {
			candidate = base + "(" + counter + ")" + extension;
			counter++;
		} while (!usedNames.add(candidate));
		return candidate;
	}

	@Override
	@Transactional(readOnly = true)
	public byte[] downloadZip(Long zipExportId) {
		ZipExport zipExport = zipExportRepository.findById(zipExportId)
				.orElseThrow(() -> new IllegalArgumentException("압축 파일을 찾을 수 없습니다: " + zipExportId));
		return fileStorageService.load(zipExport.getStorageKey());
	}
}
