package com.example.moneymanager.controller;

import com.example.moneymanager.dto.AuthDto;
import com.example.moneymanager.dto.ProfileDto;
import com.example.moneymanager.dto.ProfileDtoResponse;
import com.example.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<ProfileDtoResponse> registerProfile(@RequestBody ProfileDto profileDto) {
        ProfileDtoResponse profileDtoResponse = profileService.registerProfile(profileDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(profileDtoResponse);

    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestParam String token) {
        boolean isActivated = profileService.activateAccount(token);
        if(isActivated){
            return ResponseEntity.ok("Profile activated successfully");
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid activation token");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthDto authDto){
        try{
            if(!profileService.isAccountActive(authDto.getEmail())){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Account is not activated"));
            }
            Map<String, Object> response = profileService.authenticateAndGenerateToken(authDto);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
