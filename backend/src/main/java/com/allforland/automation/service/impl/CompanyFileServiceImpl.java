package com.allforland.automation.service.impl;

import com.allforland.automation.client.AiEngineClient;
import com.allforland.automation.client.FileParseResult;
import com.allforland.automation.common.FileDownload;
import com.allforland.automation.common.MimeTypes;
import com.allforland.automation.domain.CompanyFile;
import com.allforland.automation.domain.FileCategory;
import com.allforland.automation.domain.User;
import com.allforland.automation.dto.CompanyFileResponse;
import com.allforland.automation.repository.CompanyFileRepository;
import com.allforland.automation.service.CompanyFileService;
import com.allforland.automation.service.FileStorageService;
import com.allforland.automation.service.UserService;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CompanyFileServiceImpl implements CompanyFileService {

	private final CompanyFileRepository companyFileRepository;
	private final UserService userService;
	private final FileStorageService fileStorageService;
	private final AiEngineClient aiEngineClient;

	public CompanyFileServiceImpl(
			CompanyFileRepository companyFileRepository,
			UserService userService,
			FileStorageService fileStorageService,
			AiEngineClient aiEngineClient) {
		this.companyFileRepository = companyFileRepository;
		this.userService = userService;
		this.fileStorageService = fileStorageService;
		this.aiEngineClient = aiEngineClient;
	}

	@Override
	@Transactional
	public CompanyFileResponse upload(MultipartFile file, FileCategory category, Long userId) {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
		}
		User uploader = userService.getById(userId);
		String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
		String storageKey = fileStorageService.store(file);

		CompanyFile companyFile = new CompanyFile(
				uploader,
				file.getOriginalFilename(),
				extension != null ? extension : "unknown",
				category,
				storageKey);
		companyFile = companyFileRepository.save(companyFile);

		try {
			FileParseResult result = aiEngineClient.parseFile(file.getBytes(), file.getOriginalFilename());
			if (result.success()) {
				companyFile.markParsed(result.extractedText());
			} else {
				companyFile.markParseFailed();
			}
		} catch (IOException e) {
			companyFile.markParseFailed();
		}

		return CompanyFileResponse.from(companyFile);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CompanyFileResponse> list(FileCategory category) {
		List<CompanyFile> files = category == null
				? companyFileRepository.findAllByOrderByUploadedAtDesc()
				: companyFileRepository.findAllByCategoryOrderByUploadedAtDesc(category);
		return files.stream().map(CompanyFileResponse::from).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public FileDownload download(Long id) {
		CompanyFile file = companyFileRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("자료를 찾을 수 없습니다: " + id));
		byte[] content = fileStorageService.load(file.getStorageKey());
		return new FileDownload(content, file.getFileName(), MimeTypes.byExtension(file.getFileType()));
	}

	@Override
	@Transactional
	public void delete(Long id) {
		CompanyFile file = companyFileRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("자료를 찾을 수 없습니다: " + id));
		fileStorageService.delete(file.getStorageKey());
		companyFileRepository.delete(file);
	}
}
