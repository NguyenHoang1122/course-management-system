package com.coursemanagementsystem.repository;

import com.coursemanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserNameAndDeletedFalse(String username);
    Optional<User> findByUserName(String username);
    List<User> findByDeletedFalseOrderByIdDesc();
    List<User> findByDeletedTrueOrderByDeletedAtDesc();
    List<User> findByDeletedTrueAndDeletedAtBefore(LocalDateTime expiredAt);
    List<User> findByRole_NameAndDeletedFalseOrderByFullNameAsc(String roleName);
    List<User> findByRole_NameInAndDeletedFalseOrderByFullNameAsc(List<String> roleNames);
    Optional<User> findByIdAndDeletedFalse(Long id);
    Optional<User> findByIdAndDeletedTrue(Long id);


    boolean existsByEmail(String email);
    boolean existsByEmailAndUserNameNot(String email, String userName);
}
