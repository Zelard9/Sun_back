package org.example.sun_back.entity.user.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTOLogin {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
