package org.example.sun_back.service;

import java.util.Map;

public interface AuthService {
    String register(String username, String email, String password);

    Map<String, String> login(String email, String password);

    String refreshToken(String refreshToken);
}