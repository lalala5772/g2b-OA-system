package com.allforland.automation.dto;

import com.allforland.automation.domain.User;

public record UserResponse(Long id, String email, String name, String profileImageUrl, String role) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getProfileImageUrl(), user.getRole());
	}
}
