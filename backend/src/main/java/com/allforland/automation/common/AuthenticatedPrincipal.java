package com.allforland.automation.common;

public record AuthenticatedPrincipal(Long userId, String email, String role) {
}
