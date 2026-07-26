package com.allforland.automation.service;

import com.allforland.automation.domain.User;

public interface UserService {

	User findOrCreateByGoogle(String googleId, String email, String name, String profileImageUrl);

	User getById(Long id);
}
