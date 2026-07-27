package com.allforland.automation.service.impl;

import com.allforland.automation.domain.User;
import com.allforland.automation.repository.UserRepository;
import com.allforland.automation.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public User findOrCreateByGoogle(String googleId, String email, String name, String profileImageUrl) {
		return userRepository.findByGoogleId(googleId)
				.map(existing -> {
					existing.updateProfile(name, profileImageUrl);
					existing.touchLogin();
					return existing;
				})
				.orElseGet(() -> userRepository.save(new User(googleId, email, name, profileImageUrl)));
	}

	@Override
	@Transactional(readOnly = true)
	public User getById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));
	}
}
