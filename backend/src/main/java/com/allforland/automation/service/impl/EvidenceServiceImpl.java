package com.allforland.automation.service.impl;

import com.allforland.automation.client.AiEngineClient;
import com.allforland.automation.client.AiEngineClient.RequiredItemSuggestion;
import com.allforland.automation.common.CosineSimilarity;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvidenceServiceImpl implements EvidenceService {

	private final RequirementSetRepository requirementSetRepository;
	private final RequiredItemRepository requiredItemRepository;
	private final ZipExportRepository zipExportRepository;
	private final CompanyFileRepository companyFileRepository;
	private final UploadedFileService uploadedFileService;
	private final UserService userService;
	private final FileStorageService fileStorageService;
	private final AiEngineClient aiEngineClient;
	private final ObjectMapper objectMapper;
	private final double matchThreshold;

	public EvidenceServiceImpl(
			RequirementSetRepository requirementSetRepository,
			RequiredItemRepository requiredItemRepository,
			ZipExportRepository zipExportRepository,
			CompanyFileRepository companyFileRepository,
			UploadedFileService uploadedFileService,
			UserService userService,
			FileStorageService fileStorageService,
			AiEngineClient aiEngineClient,
			ObjectMapper objectMapper,
			@Value("${app.evidence.match-threshold}") double matchThreshold) {
		this.requirementSetRepository = requirementSetRepository;
		this.requiredItemRepository = requiredItemRepository;
		this.zipExportRepository = zipExportRepository;
		this.companyFileRepository = companyFileRepository;
		this.uploadedFileService = uploadedFileService;
		this.userService = userService;
		this.fileStorageService = fileStorageService;
		this.aiEngineClient = aiEngineClient;
		this.objectMapper = objectMapper;
		this.matchThreshold = matchThreshold;
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
				.filter(file -> file.getEmbedding() != null)
				.toList();

		List<RequiredItem> items = new ArrayList<>();
		for (RequiredItemSuggestion suggestion : suggestions) {
			RequiredItem item = requiredItemRepository.save(
					new RequiredItem(requirementSet, suggestion.name(), suggestion.description()));
			matchAgainstEvidence(item, evidenceFiles);
			items.add(item);
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

	private void matchAgainstEvidence(RequiredItem item, List<CompanyFile> evidenceFiles) {
		String queryText = item.getItemName() + " " + (item.getDescription() == null ? "" : item.getDescription());
		List<Double> queryVector = aiEngineClient.embed(queryText);
		if (queryVector.isEmpty()) {
			return;
		}

		CompanyFile bestMatch = null;
		double bestScore = 0.0;
		for (CompanyFile candidate : evidenceFiles) {
			List<Double> candidateVector = parseEmbedding(candidate.getEmbedding());
			double score = CosineSimilarity.of(queryVector, candidateVector);
			if (score > bestScore) {
				bestScore = score;
				bestMatch = candidate;
			}
		}

		if (bestMatch != null && bestScore >= matchThreshold) {
			item.applyMatch(bestMatch, bestScore);
		}
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
			for (CompanyFile file : matchedFiles) {
				zip.putNextEntry(new ZipEntry(file.getFileName()));
				zip.write(fileStorageService.load(file.getStorageKey()));
				zip.closeEntry();
			}
			zip.finish();

			String storageKey = fileStorageService.store(buffer.toByteArray(), "evidence.zip");
			int missingCount = items.size() - matchedFiles.size();
			return zipExportRepository.save(new ZipExport(requirementSet, storageKey, matchedFiles.size(), missingCount));
		} catch (IOException e) {
			return null;
		}
	}

	private List<Double> parseEmbedding(String json) {
		try {
			return objectMapper.readValue(json, new TypeReference<List<Double>>() {});
		} catch (Exception e) {
			return List.of();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public byte[] downloadZip(Long zipExportId) {
		ZipExport zipExport = zipExportRepository.findById(zipExportId)
				.orElseThrow(() -> new IllegalArgumentException("압축 파일을 찾을 수 없습니다: " + zipExportId));
		return fileStorageService.load(zipExport.getStorageKey());
	}
}
