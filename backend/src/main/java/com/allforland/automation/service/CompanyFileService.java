package com.allforland.automation.service;

import com.allforland.automation.domain.FileCategory;
import com.allforland.automation.dto.CompanyFileResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface CompanyFileService {

	CompanyFileResponse upload(MultipartFile file, FileCategory category, Long userId);

	List<CompanyFileResponse> list(FileCategory category);

	void delete(Long id);
}
