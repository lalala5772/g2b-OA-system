package com.allforland.automation.service;

import com.allforland.automation.dto.DocumentGenerateRequest;
import com.allforland.automation.dto.DocumentGenerationResponse;
import com.allforland.automation.dto.DocumentTemplateResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

	DocumentTemplateResponse uploadTemplate(MultipartFile file, String name, String fieldsSchemaJson);

	List<DocumentTemplateResponse> listTemplates();

	DocumentGenerationResponse generate(DocumentGenerateRequest request, Long userId);

	byte[] downloadGeneration(Long generationId);
}
