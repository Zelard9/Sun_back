package org.example.sun_back.controller.users;

import jakarta.validation.Valid;
import org.example.sun_back.entity.user.DTOs.UserDTO;
import org.example.sun_back.entity.user.DTOs.UserDTOLogin;
import org.example.sun_back.entity.user.DTOs.UserDTORegister;
import org.example.sun_back.entity.user.UserModel;
import org.example.sun_back.entity.user.repositories.UserRepository;
import org.example.sun_back.service.users.serviceImpl.AuthServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class UserAuthController {

    private final AuthServiceImpl authService;
    private final UserRepository userRepository;

    public UserAuthController(AuthServiceImpl authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    @Transactional
    public ResponseEntity<UserDTO> getUsername(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String email = authService.getAuthenticatedEmail();
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        return ResponseEntity.ok(new UserDTO(user));
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDTORegister request) {
        try {
            String response = authService.register(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getTypeUser()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserDTOLogin request) {
        System.out.println("📥 LOGIN REQUEST: " + request);
        System.out.println("📥 EMAIL: " + request.getEmail());
        System.out.println("📥 PASSWORD: " + request.getPassword());

        Map<String, String> accessTokens = authService.login(request.getEmail(), request.getPassword());

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessTokens.get("accessToken"));
        response.put("refreshToken", accessTokens.get("refreshToken"));

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(response);
    }


    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            String response = authService.verifyEmail(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@RequestParam String token) {
        return ResponseEntity.ok(authService.refreshToken(token));
    }

//    @PostMapping("/forgot-password")
//    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> req) {
//        try {
//            String response = authService.forgotPassword(req.get("email"));
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {return ResponseEntity.badRequest().body(e.getMessage());}
//    }

//    @PostMapping("/reset-password")
//    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> req) {
//        try {
//            String token = req.get("token");
//            String newPassword = req.get("newPassword");
//            String response = authService.resetPassword(token, newPassword);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
}