package com.dinhluong.dlmstore.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.dinhluong.dlmstore.dto.responses.LoginResponse;
import com.dinhluong.dlmstore.entity.Roles;
import com.dinhluong.dlmstore.entity.Users;
import com.dinhluong.dlmstore.exception.ValidationException;
import com.dinhluong.dlmstore.repository.RoleRepository;
import com.dinhluong.dlmstore.repository.UserRepository;
import com.dinhluong.dlmstore.utils.JwtTokenProvider;

import jakarta.transaction.Transactional;

@Service
public class GoogleService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${google.client-id}")
    private String GOOGLE_CLIENT_ID;

    @Transactional
    public LoginResponse googleLogin(String idTokenString) {

        Map<String, Object> payload = verifyGoogleToken(idTokenString);

        String email = (String) payload.get("email");
        String fullName = (String) payload.get("name");
        String picture = (String) payload.get("picture");
        String googleId = (String) payload.get("sub");

        Users user = userRepository.findByEmail(email)
                .orElseGet(() -> {

                    Roles userRole = roleRepository.findByName("USER")
                            .orElseThrow(() -> new ValidationException("ROLE USER not found"));

                    Users u = new Users();
                    u.setEmail(email);
                    u.setFullName(fullName);
                    u.setAvatarUrl(picture);
                    u.setAuthProvider("GOOGLE");
                    u.setProviderId(googleId);
                    u.setRole(userRole);
                    u.setIsEnabled(true);
                    u.setCreatedAt(LocalDateTime.now());
                    u.setUpdatedAt(LocalDateTime.now());

                    return userRepository.save(u);
                });

        String typeAccount = "GOOGLE";
        String jwt = jwtTokenProvider.generateToken(user);

        return new LoginResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                typeAccount
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> verifyGoogleToken(String idToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !GOOGLE_CLIENT_ID.equals(response.get("aud"))) {
                throw new ValidationException("Invalid Google token or audience");
            }

            return response;
        } catch (Exception e) {
            throw new ValidationException("Invalid Google token");
        }
    }
}
