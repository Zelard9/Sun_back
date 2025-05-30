package org.example.sun_back.entity.user.DTOs;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.sun_back.entity.user.applic.UserType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTORegister {
    private String username;
    private String password;
    private String email;
    @Enumerated(EnumType.STRING)
    private UserType typeUser;
}
