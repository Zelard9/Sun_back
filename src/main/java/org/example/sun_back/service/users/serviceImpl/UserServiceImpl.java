package org.example.sun_back.service.users.serviceImpl;

import org.example.sun_back.entity.user.DTOs.UserDTO;
import org.example.sun_back.entity.user.DTOs.UserDTORegister;
import org.example.sun_back.entity.user.UserModel;
import org.example.sun_back.entity.user.repositories.UserRepository;
import org.example.sun_back.service.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.example.sun_back.utils.UpdateBeanNonNull.copyNonNullProperties;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<UserModel> getAllUsers() {
        return List.of();
    }

    @Override
    public void saveUser(UserModel user) {
        userRepository.save(user);
    }

    @Override
    public void updateUser(Long id, UserModel user) {
        UserModel userToUpdate = userRepository.findById(id).orElse(null);
        copyNonNullProperties(user, userToUpdate);
        userRepository.save(userToUpdate) ;
    }

    @Override
    public UserDTO createUser(UserDTORegister user) {
        UserModel userModel = new UserModel();
        userModel.setEmail(user.getEmail());
        userModel.setUsername(user.getUsername());
        userModel.setPassword(user.getPassword());
        userModel.setTypeUser(user.getTypeUser());
        return  new UserDTO(userRepository.save(userModel));
    }

    @Override
    public UserDTO getUserById(Long id) {
        return  new UserDTO(userRepository.findById(id).orElse(null));
    }
}

