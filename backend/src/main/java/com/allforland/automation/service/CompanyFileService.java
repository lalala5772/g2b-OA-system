package com.allforland.automation.service;

import com.allforland.automation.common.FileDownload;
import com.allforland.automation.domain.FileCategory;
import com.allforland.automation.dto.CompanyFileResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface CompanyFileService {

	CompanyFileResponse upload(MultipartFile file, FileCategory category, Long userId);

	List<CompanyFileResponse> list(FileCategory category);

	FileDownload download(Long id);

	void delete(Long id);
}
