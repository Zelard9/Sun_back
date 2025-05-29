package org.example.sun_back.service;

import org.example.sun_back.config.jwtConfig.JwtUtil;
import org.example.sun_back.entity.user.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    private final CustomUserDetailService customUserDetailService;
    private final VerificationTokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;


    @Autowired
    public AuthServiceImpl(CustomUserDetailService customUserDetailService, VerificationTokenRepository tokenRepository,
                           AuthenticationManager authenticationManager,
                           PasswordEncoder passwordEncoder,
                           UserRepository userRepository,
                           JavaMailSender mailSender,
                           JwtUtil jwtUtil) {
        this.customUserDetailService = customUserDetailService;
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
    }

//    public String forgotPassword(String email) {
//        UserModel user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));
//
//        String token = UUID.randomUUID().toString();
//
//        String  resetLink = "https://diassist-production.up.railway.app/api/auth/reset-password?token=" + token;
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(user.getEmail());
//        message.setSubject("🔑 Скидання пароля");
//        message.setText("Перейдіть за посиланням, щоб скинути пароль:\n" + resetLink);
//        mailSender.send(message);
//
//        return "Лист для скидання пароля надіслано!";
//
//    }

//    public String resetPassword(String token, String newPassword) {
//        PasswordResetToken resetToken = resetTokenRepo.findByToken(token)
//                .orElseThrow(() -> new RuntimeException("Недійсний токен"));
//
//        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
//            throw new RuntimeException("Термін дії токена вичерпано");
////        }
//
//        User user = resetToken.getUser();
//        user.setPassword(passwordEncoder.encode(newPassword));
//        userRepository.save(user);
//        resetTokenRepo.delete(resetToken);
//
//        return "Пароль змінено успішно!";
//    }



    @Override
    @Transactional
    public String register(String username, String email, String password, UserType typeUser) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        UserModel user = new UserModel();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setTypeUser(typeUser);
        user.setRole(Role.ROLE_USER);
        user.setVerified(false);

        VerificationToken verificationToken = new VerificationToken(user);
        userRepository.save(user);
        tokenRepository.save(verificationToken);

        sendVerificationEmail(user.getEmail(), verificationToken.getToken());
        return "User registered! Please check your email.";
    }


    private void sendVerificationEmail(String email, String token) {

        String url = "https://sunback-production.up.railway.app/api/v1/auth/verify?token=" + token;  // update email and host for nginx!

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verification Email");
        message.setText("Click the link to verify your account in SUN: " + url);
        mailSender.send(message);
    }


    public String verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        UserModel user = verificationToken.getUser();
        if (user.isVerified()){
            return "Email already verified!";
        }

        user.setVerified(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);
        return "Email already verified!";
    }

    @Override
    public Map<String, String> login(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        UserModel user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Користувача з таким email не знайдено: " + email));
        System.out.println(user.getEmail());

        if(!user.isVerified()) {
            throw new RuntimeException("Email is not verified! Please check your email.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, password));
        UserDetails userDetails = customUserDetailService.loadUserByUsername(normalizedEmail);

        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return Map.of("accessToken", accessToken,
                "refreshToken", refreshToken);
    }

    @Override
    public String refreshToken(String refreshToken) {
        /// //ЗАКОНТРИ ЕМЕЙЛ
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        if (username == null) {
            throw new RuntimeException("Invalid refresh token");
        }

        var userOptional = userRepository.findByEmail(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new RuntimeException("Refresh token expired");
        }

        UserDetails userDetails = customUserDetailService.loadUserByUsername(username);
        return jwtUtil.generateToken(userDetails);
    }


    public String getAuthenticatedEmail() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(userDetails == null || userDetails.getUsername() == null) {
            throw new RuntimeException("User is not authenticated");
        }
        return  userDetails.getUsername();
        // throw new RuntimeException("Cannot extract username from authentication principal");
    }


}
