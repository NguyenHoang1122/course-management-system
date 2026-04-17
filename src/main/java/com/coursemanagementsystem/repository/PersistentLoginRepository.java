package com.coursemanagementsystem.repository;

import com.coursemanagementsystem.model.PersistentLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PersistentLoginRepository extends JpaRepository<PersistentLogin, String> {

    /**
     * Tìm PersistentLogin theo username
     */
    Optional<PersistentLogin> findByUsername(String username);

    /**
     * Tìm PersistentLogin theo series
     */
    Optional<PersistentLogin> findBySeries(String series);

    /**
     * Xóa PersistentLogin theo username
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PersistentLogin p WHERE p.username = :username")
    void deleteByUsername(@Param("username") String username);
}

