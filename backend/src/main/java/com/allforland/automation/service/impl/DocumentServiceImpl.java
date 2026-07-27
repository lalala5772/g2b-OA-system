package com.allforland.automation.service.impl;

import com.allforland.automation.client.AiEngineClient;
import com.allforland.automation.client.AiEngineClient.AutoFillOutcome;
import com.allforland.automation.domain.DocumentGeneration;
import com.allforland.automation.domain.FileCategory;
import com.allforland.automation.domain.User;
import com.allforland.automation.dto.DocumentGenerationResponse;
import com.allforland.automation.repository.CompanyFileRepository;
import com.allforland.automation.repository.DocumentGenerationRepository;
import com.allforland.automation.service.DocumentService;
import com.allforland.automation.service.FileStorageService;
import com.allforland.automation.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentServiceImpl implements DocumentService {

	private static final int COMPANY_TEXT_MAX_CHARS = 10000;
	private static final Set<FileCategory> COMPANY_TEXT_CATEGORIES =
			Set.of(FileCategory.DOMAIN_INTRO, FileCategory.CERTIFICATE, FileCategory.EVIDENCE);

	private final DocumentGenerationRepository documentGenerationRepository;
	private final CompanyFileRepository companyFileRepository;
	private final UserService userService;
	private final FileStorageService fileStorageService;
	private final AiEngineClient aiEngineClient;
	private final ObjectMapper objectMapper;

	public DocumentServiceImpl(
			DocumentGenerationRepository documentGenerationRepository,
			CompanyFileRepository companyFileRepository,
			UserService userService,
			FileStorageService fileStorageService,
			AiEngineClient aiEngineClient,
			ObjectMapper objectMapper) {
		this.documentGenerationRepository = documentGenerationRepository;
		this.companyFileRepository = companyFileRepository;
		this.userService = userService;
		this.fileStorageService = fileStorageService;
		this.aiEngineClient = aiEngineClient;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public DocumentGenerationResponse autoFill(MultipartFile file, Long userId) {
		User user = userService.getById(userId);
		String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.docx";

		AutoFillOutcome outcome;
		try {
			outcome = aiEngineClient.autoFillDocument(file.getBytes(), originalFilename, buildCompanyText());
		} catch (Exception ex) {
			outcome = new AutoFillOutcome(null, Map.of());
		}

		DocumentGeneration generation = documentGenerationRepository.save(
				new DocumentGeneration(originalFilename, user, writeJson(outcome.filledFields())));

		if (outcome.filledDocument() != null) {
			String outputKey = fileStorageService.store(outcome.filledDocument(), originalFilename + "-filled.docx");
			generation.markSuccess(outputKey);
		}

		return DocumentGenerationResponse.of(generation, outcome.filledFields());
	}

	@Override
	@Transactional(readOnly = true)
	public byte[] downloadGeneration(Long generationId, Long userId) {
		DocumentGeneration generation = documentGenerationRepository
				.findById(generationId)
				.filter(candidate -> candidate.getUser().getId().equals(userId))
				.orElseThrow(() -> new IllegalArgumentException("생성 결과를 찾을 수 없습니다: " + generationId));
		if (generation.getOutputStorageKey() == null) {
			throw new IllegalArgumentException("아직 생성이 완료되지 않았습니다.");
		}
		return fileStorageService.load(generation.getOutputStorageKey());
	}

	private String buildCompanyText() {
		// CERTIFICATE/EVIDENCE(사업자등록증, 등기사항전부증명서 등)는 짧고 대표자명·사업자등록번호·
		// 소재지 같은 등록 정보를 정확히 담고 있어 먼저 배치 — 서술형인 DOMAIN_INTRO는 뒤로 밀려도
		// 캡에 걸리는 쪽이 되도록 한다.
		String text = companyFileRepository.findAll().stream()
				.filter(file -> COMPANY_TEXT_CATEGORIES.contains(file.getCategory()))
				.sorted(Comparator.comparing(file -> file.getCategory() == FileCategory.DOMAIN_INTRO ? 1 : 0))
				.map(file -> file.getExtractedText() == null ? "" : file.getExtractedText())
				.collect(Collectors.joining("\n\n"));
		return text.length() > COMPANY_TEXT_MAX_CHARS ? text.substring(0, COMPANY_TEXT_MAX_CHARS) : text;
	}

	private String writeJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception e) {
			return "{}";
		}
	}
}
