package com.allforland.automation.service;

import com.allforland.automation.domain.FilePurpose;
import com.allforland.automation.domain.UploadedFile;
import com.allforland.automation.dto.UploadedFileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UploadedFileService {

	UploadedFileResponse upload(MultipartFile file, FilePurpose purpose, Long userId);

	UploadedFile getById(Long id);
}
