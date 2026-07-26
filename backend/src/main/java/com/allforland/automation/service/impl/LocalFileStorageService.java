package com.allforland.automation.service.impl;

import com.allforland.automation.service.FileStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Dev-stage storage backend. Swap for an S3-backed implementation of {@link FileStorageService}
 * at deploy time without touching callers.
 */
@Service
public class LocalFileStorageService implements FileStorageService {

	private final Path baseDir;

	public LocalFileStorageService(@Value("${app.file-storage.base-dir}") String baseDir) {
		this.baseDir = Path.of(baseDir);
		try {
			Files.createDirectories(this.baseDir);
		} catch (IOException e) {
			throw new IllegalStateException("파일 저장 디렉토리를 생성할 수 없습니다: " + baseDir, e);
		}
	}

	@Override
	public String store(MultipartFile file) {
		String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
		String storageKey = newStorageKey(extension);
		Path target = baseDir.resolve(storageKey);
		try {
			file.transferTo(target);
		} catch (IOException e) {
			throw new IllegalStateException("파일 저장에 실패했습니다.", e);
		}
		return storageKey;
	}

	@Override
	public String store(byte[] content, String suggestedFilename) {
		String extension = StringUtils.getFilenameExtension(suggestedFilename);
		String storageKey = newStorageKey(extension);
		try {
			Files.write(baseDir.resolve(storageKey), content);
		} catch (IOException e) {
			throw new IllegalStateException("파일 저장에 실패했습니다.", e);
		}
		return storageKey;
	}

	@Override
	public byte[] load(String storageKey) {
		try {
			return Files.readAllBytes(baseDir.resolve(storageKey));
		} catch (IOException e) {
			throw new IllegalStateException("파일을 읽을 수 없습니다: " + storageKey, e);
		}
	}

	@Override
	public void delete(String storageKey) {
		try {
			Files.deleteIfExists(baseDir.resolve(storageKey));
		} catch (IOException e) {
			throw new IllegalStateException("파일 삭제에 실패했습니다.", e);
		}
	}

	private String newStorageKey(String extension) {
		return UUID.randomUUID() + (extension != null ? "." + extension : "");
	}
}
