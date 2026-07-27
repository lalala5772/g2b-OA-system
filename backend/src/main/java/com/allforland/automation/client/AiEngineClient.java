package com.allforland.automation.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AiEngineClient {

	private static final Logger log = LoggerFactory.getLogger(AiEngineClient.class);

	private final WebClient webClient;
	private final Duration timeout;
	private final Duration longTimeout;

	public AiEngineClient(
			WebClient.Builder webClientBuilder,
			@Value("${ai-engine.base-url}") String baseUrl,
			@Value("${ai-engine.api-key}") String apiKey,
			@Value("${ai-engine.timeout-seconds}") long timeoutSeconds,
			@Value("${ai-engine.long-timeout-seconds}") long longTimeoutSeconds) {
		this.webClient = webClientBuilder
				.baseUrl(baseUrl)
				.defaultHeader("X-API-Key", apiKey)
				.build();
		this.timeout = Duration.ofSeconds(timeoutSeconds);
		this.longTimeout = Duration.ofSeconds(longTimeoutSeconds);
	}

	/** LLM-backed calls (bid scans, ...) run longer than a plain file parse. */
	public BidScanOutcome scanBids(
			List<String> keywords,
			String companyProfile,
			double eligibilityThreshold,
			LocalDate startDate,
			LocalDate endDate) {
		try {
			ScanResponse response = webClient.post()
					.uri("/bids/scan")
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue(new ScanRequest(
							keywords,
							companyProfile,
							eligibilityThreshold,
							startDate != null ? startDate.toString() : null,
							endDate != null ? endDate.toString() : null))
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<ScanResponse>() {})
					.timeout(longTimeout)
					.block();
			if (response == null) {
				return BidScanOutcome.empty(startDate, endDate);
			}
			return new BidScanOutcome(
					response.results(),
					response.fetched(),
					LocalDate.parse(response.rangeStart()),
					LocalDate.parse(response.rangeEnd()),
					response.judged(),
					response.unjudged());
		} catch (Exception ex) {
			log.error("ai-engine 나라장터 스캔 호출 실패 (startDate={}, endDate={})", startDate, endDate, ex);
			return BidScanOutcome.empty(startDate, endDate);
		}
	}

	private record ScanRequest(
			List<String> keywords,
			@JsonProperty("company_profile") String companyProfile,
			@JsonProperty("eligibility_threshold") double eligibilityThreshold,
			@JsonProperty("start_date") String startDate,
			@JsonProperty("end_date") String endDate) {
	}

	private record ScanResponse(
			List<BidScanResult> results,
			int fetched,
			@JsonProperty("range_start") String rangeStart,
			@JsonProperty("range_end") String rangeEnd,
			int judged,
			int unjudged) {
	}

	public AutoFillOutcome autoFillDocument(byte[] fileBytes, String filename, String companyText) {
		MultipartBodyBuilder builder = new MultipartBodyBuilder();
		builder.part("file", new ByteArrayResource(fileBytes) {
			@Override
			public String getFilename() {
				return filename;
			}
		});
		builder.part("company_text", companyText);

		try {
			AutoFillResponse response = webClient.post()
					.uri("/documents/auto-fill")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.bodyValue(builder.build())
					.retrieve()
					.bodyToMono(AutoFillResponse.class)
					.timeout(longTimeout)
					.block();
			if (response == null) {
				return new AutoFillOutcome(null, Map.of());
			}
			return new AutoFillOutcome(
					Base64.getDecoder().decode(response.filledDocumentBase64()), response.filledFields());
		} catch (Exception ex) {
			log.error("ai-engine 문서 자동채움 호출 실패 (filename={})", filename, ex);
			return new AutoFillOutcome(null, Map.of());
		}
	}

	public record AutoFillOutcome(byte[] filledDocument, Map<String, String> filledFields) {
	}

	private record AutoFillResponse(
			@JsonProperty("filled_document_base64") String filledDocumentBase64,
			@JsonProperty("filled_fields") Map<String, String> filledFields) {
	}

	public List<RequiredItemSuggestion> extractRequirements(String requirementText) {
		try {
			RequirementsResponse response = webClient.post()
					.uri("/evidence/extract-requirements")
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue(Map.of("requirement_text", requirementText))
					.retrieve()
					.bodyToMono(RequirementsResponse.class)
					.timeout(longTimeout)
					.block();
			return response == null ? List.of() : response.items();
		} catch (Exception ex) {
			log.error("ai-engine 제출서류 추출 호출 실패", ex);
			return List.of();
		}
	}

	private record RequirementsResponse(List<RequiredItemSuggestion> items) {
	}

	public record RequiredItemSuggestion(String name, String description) {
	}

	/**
	 * Hands Claude the full text of both lists so it judges real document-type equivalence
	 * (e.g. 사업자등록증 vs 중소기업확인서) instead of a bare cosine-similarity score, which is
	 * what previously let two different required items get matched to the same file.
	 */
	public List<EvidenceItemMatch> matchEvidenceItems(
			List<RequiredItemSuggestion> items, List<EvidenceFileCandidate> evidenceFiles) {
		try {
			MatchItemsResponse response = webClient.post()
					.uri("/evidence/match-items")
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue(new MatchItemsRequest(items, evidenceFiles))
					.retrieve()
					.bodyToMono(MatchItemsResponse.class)
					.timeout(longTimeout)
					.block();
			return response == null ? List.of() : response.matches();
		} catch (Exception ex) {
			log.error("ai-engine 증빙 매칭 호출 실패 (itemCount={}, fileCount={})", items.size(), evidenceFiles.size(), ex);
			return List.of();
		}
	}

	public record EvidenceFileCandidate(
			Long id, String filename, @JsonProperty("text") String text) {
	}

	public record EvidenceItemMatch(
			@JsonProperty("evidence_file_id") Long evidenceFileId, String reason) {
	}

	private record MatchItemsRequest(
			List<RequiredItemSuggestion> items, @JsonProperty("evidence_files") List<EvidenceFileCandidate> evidenceFiles) {
	}

	private record MatchItemsResponse(List<EvidenceItemMatch> matches) {
	}

	public FileParseResult parseFile(byte[] content, String filename) {
		MultipartBodyBuilder builder = new MultipartBodyBuilder();
		builder.part("file", new ByteArrayResource(content) {
			@Override
			public String getFilename() {
				return filename;
			}
		});

		try {
			ParseResponse response = webClient.post()
					.uri("/files/parse")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.bodyValue(builder.build())
					.retrieve()
					.bodyToMono(ParseResponse.class)
					.timeout(timeout)
					.block();

			if (response == null) {
				return new FileParseResult(false, null, "AI 엔진 응답이 비어 있습니다.");
			}
			return new FileParseResult(
					"success".equals(response.status()), response.extractedText(), response.message());
		} catch (Exception ex) {
			log.error("ai-engine 파일 파싱 호출 실패 (filename={})", filename, ex);
			return new FileParseResult(false, null, "AI 엔진 호출에 실패했습니다: " + ex.getMessage());
		}
	}

	private record ParseResponse(
			String status,
			@JsonProperty("extracted_text") String extractedText,
			String message) {
	}
}
