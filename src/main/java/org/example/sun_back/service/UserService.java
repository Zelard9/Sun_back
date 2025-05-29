package org.example.sun_back.service;

import org.example.sun_back.entity.user.DTOs.UserDTO;
import org.example.sun_back.entity.user.DTOs.UserDTORegister;
import org.example.sun_back.entity.user.UserModel;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserService {
    List<UserModel> getAllUsers();
    void saveUser(UserModel user);
    void updateUser(Long id, UserModel user);
    UserDTO createUser(UserDTORegister user);
    UserDTO getUserById(Long id);
}
