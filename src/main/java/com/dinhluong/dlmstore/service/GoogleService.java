package com.dinhluong.dlmstore.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value; // Không cần check aud thủ công nữa
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

    @Transactional
    public LoginResponse googleLogin(String accessToken) { 

        
        Map<String, Object> payload = getUserInfo(accessToken);

        
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


    private Map<String, Object> getUserInfo(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
          
            String url = "https://www.googleapis.com/oauth2/v3/userinfo";

            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                Map.class
            );

            return response.getBody();

        } catch (Exception e) {
            e.printStackTrace(); 
            throw new ValidationException("Invalid Google Access Token");
        }
    }
}