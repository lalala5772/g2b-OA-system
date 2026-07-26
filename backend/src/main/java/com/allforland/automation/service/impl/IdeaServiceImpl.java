package com.allforland.automation.service.impl;

import com.allforland.automation.client.AiEngineClient;
import com.allforland.automation.client.AiEngineClient.GeneratedIdea;
import com.allforland.automation.domain.CompanyFile;
import com.allforland.automation.domain.ContestIdea;
import com.allforland.automation.domain.FileCategory;
import com.allforland.automation.domain.IdeaRequest;
import com.allforland.automation.domain.IdeaRequestCompanyFile;
import com.allforland.automation.domain.UploadedFile;
import com.allforland.automation.domain.User;
import com.allforland.automation.dto.ContestIdeaResponse;
import com.allforland.automation.dto.IdeaGenerateRequest;
import com.allforland.automation.dto.IdeaGenerateResponse;
import com.allforland.automation.repository.CompanyFileRepository;
import com.allforland.automation.repository.ContestIdeaRepository;
import com.allforland.automation.repository.IdeaRequestCompanyFileRepository;
import com.allforland.automation.repository.IdeaRequestRepository;
import com.allforland.automation.service.IdeaService;
import com.allforland.automation.service.UploadedFileService;
import com.allforland.automation.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdeaServiceImpl implements IdeaService {

	private final IdeaRequestRepository ideaRequestRepository;
	private final IdeaRequestCompanyFileRepository ideaRequestCompanyFileRepository;
	private final ContestIdeaRepository contestIdeaRepository;
	private final CompanyFileRepository companyFileRepository;
	private final UploadedFileService uploadedFileService;
	private final UserService userService;
	private final AiEngineClient aiEngineClient;
	private final String claudeModel;

	public IdeaServiceImpl(
			IdeaRequestRepository ideaRequestRepository,
			IdeaRequestCompanyFileRepository ideaRequestCompanyFileRepository,
			ContestIdeaRepository contestIdeaRepository,
			CompanyFileRepository companyFileRepository,
			UploadedFileService uploadedFileService,
			UserService userService,
			AiEngineClient aiEngineClient,
			@Value("${claude.model}") String claudeModel) {
		this.ideaRequestRepository = ideaRequestRepository;
		this.ideaRequestCompanyFileRepository = ideaRequestCompanyFileRepository;
		this.contestIdeaRepository = contestIdeaRepository;
		this.companyFileRepository = companyFileRepository;
		this.uploadedFileService = uploadedFileService;
		this.userService = userService;
		this.aiEngineClient = aiEngineClient;
		this.claudeModel = claudeModel;
	}

	@Override
	@Transactional
	public IdeaGenerateResponse generate(IdeaGenerateRequest request, Long userId) {
		User user = userService.getById(userId);
		UploadedFile contestFile = uploadedFileService.getById(request.contestFileId());

		IdeaRequest ideaRequest = ideaRequestRepository.save(new IdeaRequest(user, contestFile));

		List<CompanyFile> domainFiles = companyFileRepository
				.findAllByCategoryOrderByUploadedAtDesc(FileCategory.DOMAIN_INTRO)
				.stream()
				.limit(3)
				.toList();
		domainFiles.forEach(file -> ideaRequestCompanyFileRepository.save(new IdeaRequestCompanyFile(ideaRequest, file)));

		if (domainFiles.isEmpty() || contestFile.getExtractedText() == null) {
			ideaRequest.markFailed();
			return new IdeaGenerateResponse(ideaRequest.getId(), ideaRequest.getStatus(), List.of());
		}

		List<String> companyTexts = domainFiles.stream().map(CompanyFile::getExtractedText).toList();
		List<GeneratedIdea> generated = aiEngineClient.generateIdeas(contestFile.getExtractedText(), companyTexts);

		if (generated.isEmpty()) {
			ideaRequest.markFailed();
			return new IdeaGenerateResponse(ideaRequest.getId(), ideaRequest.getStatus(), List.of());
		}

		List<ContestIdeaResponse> saved = generated.stream()
				.map(idea -> contestIdeaRepository.save(
						new ContestIdea(ideaRequest, idea.title(), idea.content(), idea.relevanceScore(), claudeModel)))
				.map(ContestIdeaResponse::from)
				.toList();

		ideaRequest.markCompleted();
		return new IdeaGenerateResponse(ideaRequest.getId(), ideaRequest.getStatus(), saved);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ContestIdeaResponse> recentIdeas() {
		return contestIdeaRepository.findTop20ByOrderByGeneratedAtDesc().stream().map(ContestIdeaResponse::from).toList();
	}
}
