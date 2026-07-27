package com.allforland.automation.service;

import com.allforland.automation.dto.DocumentGenerationResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

	DocumentGenerationResponse autoFill(MultipartFile file, Long userId);

	byte[] downloadGeneration(Long generationId, Long userId);
}
