package com.allforland.automation.service.impl;

import com.allforland.automation.client.AiEngineClient;
import com.allforland.automation.client.FileParseResult;
import com.allforland.automation.domain.FilePurpose;
import com.allforland.automation.domain.UploadedFile;
import com.allforland.automation.domain.User;
import com.allforland.automation.dto.UploadedFileResponse;
import com.allforland.automation.repository.UploadedFileRepository;
import com.allforland.automation.service.FileStorageService;
import com.allforland.automation.service.UploadedFileService;
import com.allforland.automation.service.UserService;
import java.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadedFileServiceImpl implements UploadedFileService {

	private final UploadedFileRepository uploadedFileRepository;
	private final UserService userService;
	private final FileStorageService fileStorageService;
	private final AiEngineClient aiEngineClient;

	public UploadedFileServiceImpl(
			UploadedFileRepository uploadedFileRepository,
			UserService userService,
			FileStorageService fileStorageService,
			AiEngineClient aiEngineClient) {
		this.uploadedFileRepository = uploadedFileRepository;
		this.userService = userService;
		this.fileStorageService = fileStorageService;
		this.aiEngineClient = aiEngineClient;
	}

	@Override
	@Transactional
	public UploadedFileResponse upload(MultipartFile file, FilePurpose purpose, Long userId) {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
		}
		User uploader = userService.getById(userId);
		String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
		String storageKey = fileStorageService.store(file);

		UploadedFile uploaded = new UploadedFile(
				uploader,
				file.getOriginalFilename(),
				extension != null ? extension : "unknown",
				purpose,
				storageKey);
		uploaded = uploadedFileRepository.save(uploaded);

		try {
			FileParseResult result = aiEngineClient.parseFile(file.getBytes(), file.getOriginalFilename());
			if (result.success()) {
				uploaded.markParsed(result.extractedText());
			} else {
				uploaded.markParseFailed();
			}
		} catch (IOException e) {
			uploaded.markParseFailed();
		}

		return UploadedFileResponse.from(uploaded);
	}

	@Override
	@Transactional(readOnly = true)
	public UploadedFile getById(Long id) {
		return uploadedFileRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다: " + id));
	}
}
