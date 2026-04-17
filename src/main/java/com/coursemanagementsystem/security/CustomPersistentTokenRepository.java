package com.coursemanagementsystem.security;

import com.coursemanagementsystem.model.PersistentLogin;
import com.coursemanagementsystem.repository.PersistentLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Component
public class CustomPersistentTokenRepository implements PersistentTokenRepository {

    @Autowired
    private PersistentLoginRepository persistentLoginRepository;

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        PersistentLogin persistentLogin = new PersistentLogin();
        persistentLogin.setSeries(token.getSeries());
        persistentLogin.setUsername(token.getUsername());
        persistentLogin.setToken(token.getTokenValue());
        persistentLogin.setLastUsed(convertToLocalDateTime(token.getDate()));

        persistentLoginRepository.save(persistentLogin);
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        Optional<PersistentLogin> persistentLogin = persistentLoginRepository.findBySeries(series);

        if (persistentLogin.isPresent()) {
            PersistentLogin login = persistentLogin.get();
            login.setToken(tokenValue);
            login.setLastUsed(convertToLocalDateTime(lastUsed));
            persistentLoginRepository.save(login);
        }
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        Optional<PersistentLogin> persistentLogin = persistentLoginRepository.findBySeries(seriesId);

        if (persistentLogin.isPresent()) {
            PersistentLogin login = persistentLogin.get();
            return new PersistentRememberMeToken(
                    login.getUsername(),
                    login.getSeries(),
                    login.getToken(),
                    convertToDate(login.getLastUsed())
            );
        }

        return null;
    }

    @Override
    public void removeUserTokens(String username) {
        persistentLoginRepository.deleteByUsername(username);
    }

    /**
     * Convert LocalDateTime to Date
     */
    private Date convertToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return java.sql.Timestamp.valueOf(localDateTime);
    }

    /**
     * Convert Date to LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}

