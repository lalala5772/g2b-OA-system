package com.allforland.automation.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

	/**
	 * Persists the file and returns a storage key that can later be used to locate/delete it.
	 */
	String store(MultipartFile file);

	/** Same as {@link #store(MultipartFile)} but for bytes generated in-process (e.g. a filled document). */
	String store(byte[] content, String suggestedFilename);

	byte[] load(String storageKey);

	void delete(String storageKey);
}
