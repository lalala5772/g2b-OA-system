package com.allforland.automation.repository;

import com.allforland.automation.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByGoogleId(String googleId);
}
