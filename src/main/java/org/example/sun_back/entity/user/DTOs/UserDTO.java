package org.example.sun_back.entity.user.DTOs;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.sun_back.entity.user.UserModel;
import org.example.sun_back.entity.user.applic.UserType;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private String email;
    @Enumerated(EnumType.STRING)
    private UserType typeUser;
    private boolean isVerified;


    public UserDTO(UserModel user) {
        this.id = user.getId();
        this.username = user.getUserNameDisplay();
        this.email = user.getEmail();
        this.isVerified = user.isVerified();
    }
}
