package com.allforland.automation.repository;

import com.allforland.automation.domain.CompanyFile;
import com.allforland.automation.domain.FileCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyFileRepository extends JpaRepository<CompanyFile, Long> {

	List<CompanyFile> findAllByOrderByUploadedAtDesc();

	List<CompanyFile> findAllByCategoryOrderByUploadedAtDesc(FileCategory category);

	long countByCategory(FileCategory category);
}
