package com.dinhluong.dlmstore.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
public class FacebookService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

   @Transactional
    public LoginResponse facebookLogin(String accessToken) {
       
        Map<String, Object> payload = getFacebookUserInfo(accessToken);

        String facebookId = (String) payload.get("id");
        String name = (String) payload.get("name");
        String email = (String) payload.get("email");
        
        String picture = null;
        if (payload.containsKey("picture")) {
            Map<String, Object> pictureObj = (Map<String, Object>) payload.get("picture");
            Map<String, Object> dataObj = (Map<String, Object>) pictureObj.get("data");
            picture = (String) dataObj.get("url");
        }

        if (email == null) {
            email = facebookId + "@facebook.com";
        }
        
        
        final String finalEmail = email; 
        final String finalPicture = picture; 
       
        Users user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    Roles userRole = roleRepository.findByName("USER")
                            .orElseThrow(() -> new ValidationException("ROLE USER not found"));

                    Users u = new Users();
                    u.setEmail(finalEmail); 
                    u.setFullName(name);
                    
                
                    u.setAvatarUrl(finalPicture); 
                    
                    
                    u.setAuthProvider("FACEBOOK");
                    u.setProviderId(facebookId);
                    u.setRole(userRole);
                    u.setIsEnabled(true);
                    u.setCreatedAt(LocalDateTime.now());
                    u.setUpdatedAt(LocalDateTime.now());

                    return userRepository.save(u);
                });

        String jwt = jwtTokenProvider.generateToken(user);

        return new LoginResponse(jwt, user.getId(), user.getEmail(), user.getFullName(), user.getAvatarUrl(), "FACEBOOK");
    }

    private Map<String, Object> getFacebookUserInfo(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=" + accessToken;
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            throw new ValidationException("Invalid Facebook Access Token");
        }
    }
}