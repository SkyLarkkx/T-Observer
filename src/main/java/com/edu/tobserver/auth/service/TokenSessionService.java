package com.edu.tobserver.auth.service;

import com.edu.tobserver.common.context.LoginUser;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class TokenSessionService {

    private final Map<String, LoginUser> sessions = new ConcurrentHashMap<>();

    public String create(LoginUser loginUser) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, loginUser);
        return token;
    }

    public LoginUser get(String token) {
        return sessions.get(token);
    }
}
