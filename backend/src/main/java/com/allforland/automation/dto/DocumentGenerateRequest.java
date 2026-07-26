package com.allforland.automation.dto;

import java.util.Map;

public record DocumentGenerateRequest(Long templateId, Map<String, String> fieldValues) {
}
