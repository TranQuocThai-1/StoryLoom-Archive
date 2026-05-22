package com.storyloom.archive.repository;

import com.storyloom.archive.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    Optional<User> findByScreenName(String screenName);
}
