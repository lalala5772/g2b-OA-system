package com.allforland.automation.service;

import com.allforland.automation.dto.EvidenceAnalysisResponse;

public interface EvidenceService {

	EvidenceAnalysisResponse analyze(Long uploadedFileId, Long userId);

	byte[] downloadZip(Long zipExportId);
}
