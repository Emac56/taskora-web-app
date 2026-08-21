package com.taskora.api.features.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskora.api.features.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}