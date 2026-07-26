package com.allforland.automation.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AiEngineClient {

	private final WebClient webClient;
	private final Duration timeout;

	public AiEngineClient(
			WebClient.Builder webClientBuilder,
			@Value("${ai-engine.base-url}") String baseUrl,
			@Value("${ai-engine.api-key}") String apiKey,
			@Value("${ai-engine.timeout-seconds}") long timeoutSeconds) {
		this.webClient = webClientBuilder
				.baseUrl(baseUrl)
				.defaultHeader("X-API-Key", apiKey)
				.build();
		this.timeout = Duration.ofSeconds(timeoutSeconds);
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
			return new FileParseResult(false, null, "AI 엔진 호출에 실패했습니다: " + ex.getMessage());
		}
	}

	private record ParseResponse(
			String status,
			@JsonProperty("extracted_text") String extractedText,
			String message) {
	}
}
