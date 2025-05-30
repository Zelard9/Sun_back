package org.example.sun_back.service.users.service;

import org.example.sun_back.entity.user.applic.UserType;

import java.util.Map;

public interface AuthService {
    String register(String username, String email, String password,  UserType typeUser, String phonenumber);

    Map<String, String> login(String email, String password);

    String refreshToken(String refreshToken);
}