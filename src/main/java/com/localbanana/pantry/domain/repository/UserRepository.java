// UserRepository.java
package com.localbanana.pantry.domain.repository;

import com.localbanana.pantry.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}