package com.allforland.automation.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

	/**
	 * Persists the file and returns a storage key that can later be used to locate/delete it.
	 */
	String store(MultipartFile file);

	void delete(String storageKey);
}
