package org.hartford.binsure.repository;

import org.hartford.binsure.entity.User;
import org.hartford.binsure.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByIsActiveTrueOrderByCreatedAtDesc();
}
