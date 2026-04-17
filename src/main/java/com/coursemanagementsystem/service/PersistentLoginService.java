package com.coursemanagementsystem.service;

import com.coursemanagementsystem.model.PersistentLogin;
import com.coursemanagementsystem.repository.PersistentLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PersistentLoginService {

    @Autowired
    private PersistentLoginRepository persistentLoginRepository;

    /**
     * Tạo mới một persistent login token cho user
     */
    public PersistentLogin createPersistentLogin(String username) {
        String series = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString();

        PersistentLogin persistentLogin = new PersistentLogin();
        persistentLogin.setSeries(series);
        persistentLogin.setUsername(username);
        persistentLogin.setToken(token);
        persistentLogin.setLastUsed(LocalDateTime.now());

        return persistentLoginRepository.save(persistentLogin);
    }

    /**
     * Tìm persistent login theo series
     */
    public Optional<PersistentLogin> getPersistentLoginBySeries(String series) {
        return persistentLoginRepository.findBySeries(series);
    }

    /**
     * Tìm persistent login theo username
     */
    public Optional<PersistentLogin> getPersistentLoginByUsername(String username) {
        return persistentLoginRepository.findByUsername(username);
    }

    /**
     * Cập nhật thời gian sử dụng cuối cùng của persistent login
     */
    public PersistentLogin updateLastUsed(String series) {
        Optional<PersistentLogin> persistentLogin = persistentLoginRepository.findBySeries(series);

        if (persistentLogin.isPresent()) {
            PersistentLogin login = persistentLogin.get();
            login.setLastUsed(LocalDateTime.now());
            return persistentLoginRepository.save(login);
        }

        return null;
    }

    /**
     * Xóa persistent login theo series
     */
    public void deletePersistentLoginBySeries(String series) {
        persistentLoginRepository.deleteById(series);
    }

    /**
     * Xóa persistent login theo username
     */
    public void deletePersistentLoginByUsername(String username) {
        persistentLoginRepository.deleteByUsername(username);
    }

    /**
     * Xóa tất cả persistent login
     */
    public void deleteAllPersistentLogins() {
        persistentLoginRepository.deleteAll();
    }
}

