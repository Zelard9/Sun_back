package org.example.sun_back.service;

import org.example.sun_back.entity.user.UserType;

import java.util.Map;

public interface AuthService {
    String register(String username, String email, String password,  UserType typeUser);

    Map<String, String> login(String email, String password);

    String refreshToken(String refreshToken);
}