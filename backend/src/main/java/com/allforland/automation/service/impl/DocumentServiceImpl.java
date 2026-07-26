package com.allforland.automation.service.impl;

import com.allforland.automation.client.AiEngineClient;
import com.allforland.automation.domain.DocumentGeneration;
import com.allforland.automation.domain.DocumentTemplate;
import com.allforland.automation.domain.FileCategory;
import com.allforland.automation.domain.User;
import com.allforland.automation.dto.DocumentFieldSchema;
import com.allforland.automation.dto.DocumentGenerateRequest;
import com.allforland.automation.dto.DocumentGenerationResponse;
import com.allforland.automation.dto.DocumentTemplateResponse;
import com.allforland.automation.repository.CompanyFileRepository;
import com.allforland.automation.repository.DocumentGenerationRepository;
import com.allforland.automation.repository.DocumentTemplateRepository;
import com.allforland.automation.service.DocumentService;
import com.allforland.automation.service.FileStorageService;
import com.allforland.automation.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentServiceImpl implements DocumentService {

	private static final int COMPANY_TEXT_MAX_CHARS = 6000;

	private final DocumentTemplateRepository documentTemplateRepository;
	private final DocumentGenerationRepository documentGenerationRepository;
	private final CompanyFileRepository companyFileRepository;
	private final UserService userService;
	private final FileStorageService fileStorageService;
	private final AiEngineClient aiEngineClient;
	private final ObjectMapper objectMapper;

	public DocumentServiceImpl(
			DocumentTemplateRepository documentTemplateRepository,
			DocumentGenerationRepository documentGenerationRepository,
			CompanyFileRepository companyFileRepository,
			UserService userService,
			FileStorageService fileStorageService,
			AiEngineClient aiEngineClient,
			ObjectMapper objectMapper) {
		this.documentTemplateRepository = documentTemplateRepository;
		this.documentGenerationRepository = documentGenerationRepository;
		this.companyFileRepository = companyFileRepository;
		this.userService = userService;
		this.fileStorageService = fileStorageService;
		this.aiEngineClient = aiEngineClient;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public DocumentTemplateResponse uploadTemplate(MultipartFile file, String name, String fieldsSchemaJson) {
		List<DocumentFieldSchema> fields = parseFields(fieldsSchemaJson);
		String storageKey = fileStorageService.store(file);
		DocumentTemplate template = documentTemplateRepository.save(
				new DocumentTemplate(name, storageKey, fieldsSchemaJson));
		return DocumentTemplateResponse.of(template, fields);
	}

	@Override
	@Transactional(readOnly = true)
	public List<DocumentTemplateResponse> listTemplates() {
		return documentTemplateRepository.findAll().stream()
				.map(template -> DocumentTemplateResponse.of(template, parseFields(template.getFieldsSchemaJson())))
				.toList();
	}

	@Override
	@Transactional
	public DocumentGenerationResponse generate(DocumentGenerateRequest request, Long userId) {
		DocumentTemplate template = documentTemplateRepository.findById(request.templateId())
				.orElseThrow(() -> new IllegalArgumentException("템플릿을 찾을 수 없습니다: " + request.templateId()));
		User user = userService.getById(userId);

		List<DocumentFieldSchema> fields = parseFields(template.getFieldsSchemaJson());
		List<String> autoKeys = fields.stream().filter(DocumentFieldSchema::auto).map(DocumentFieldSchema::key).toList();

		Map<String, String> autoFilled = autoKeys.isEmpty() ? Map.of() : aiEngineClient.extractFields(buildCompanyText(), autoKeys);

		Map<String, String> merged = new HashMap<>(autoFilled);
		if (request.fieldValues() != null) {
			merged.putAll(request.fieldValues());
		}

		DocumentGeneration generation = documentGenerationRepository.save(
				new DocumentGeneration(template, user, writeJson(merged)));

		byte[] templateBytes = fileStorageService.load(template.getStorageKey());
		byte[] filled = aiEngineClient.fillDocument(templateBytes, template.getName() + ".docx", merged);
		if (filled != null) {
			String outputKey = fileStorageService.store(filled, template.getName() + "-filled.docx");
			generation.markSuccess(outputKey);
		}

		return DocumentGenerationResponse.of(generation, autoFilled);
	}

	@Override
	@Transactional(readOnly = true)
	public byte[] downloadGeneration(Long generationId) {
		DocumentGeneration generation = documentGenerationRepository.findById(generationId)
				.orElseThrow(() -> new IllegalArgumentException("생성 결과를 찾을 수 없습니다: " + generationId));
		if (generation.getOutputStorageKey() == null) {
			throw new IllegalArgumentException("아직 생성이 완료되지 않았습니다.");
		}
		return fileStorageService.load(generation.getOutputStorageKey());
	}

	private String buildCompanyText() {
		String text = companyFileRepository.findAll().stream()
				.filter(file -> file.getCategory() == FileCategory.DOMAIN_INTRO || file.getCategory() == FileCategory.CERTIFICATE)
				.map(file -> file.getExtractedText() == null ? "" : file.getExtractedText())
				.collect(Collectors.joining("\n\n"));
		return text.length() > COMPANY_TEXT_MAX_CHARS ? text.substring(0, COMPANY_TEXT_MAX_CHARS) : text;
	}

	private List<DocumentFieldSchema> parseFields(String json) {
		if (!StringUtils.hasText(json)) {
			return List.of();
		}
		try {
			return objectMapper.readValue(json, new TypeReference<List<DocumentFieldSchema>>() {});
		} catch (Exception e) {
			return List.of();
		}
	}

	private String writeJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception e) {
			return "{}";
		}
	}
}
