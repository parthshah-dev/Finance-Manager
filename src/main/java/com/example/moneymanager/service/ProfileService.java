package com.example.moneymanager.service;

import com.example.moneymanager.dto.AuthDto;
import com.example.moneymanager.dto.ProfileDto;
import com.example.moneymanager.dto.ProfileDtoResponse;
import com.example.moneymanager.entity.Profile;
import com.example.moneymanager.repository.ProfileRepository;
import com.example.moneymanager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.activation.url}")
    private String activationUrl;

    @Value("${app.email.enabled}")
    private boolean isEmailEnabled;

    @Transactional
    public ProfileDtoResponse registerProfile(ProfileDto profileDto){
        Profile profile = modelMapper.map(profileDto, Profile.class);

        Optional<Profile> oldProfile = profileRepository.findByEmail(profileDto.getEmail());
        if(oldProfile.isPresent()){
            throw new RuntimeException("Email already exists");
        }

        if (isEmailEnabled) {
            profile.setActivationToken(UUID.randomUUID().toString());
        } else {
            profile.setIsActive(true);
        }

        profile.setPassword(passwordEncoder.encode(profile.getPassword()));
        Profile savedProfile = profileRepository.save(profile);

        if (isEmailEnabled) {
            //send activation link
            String activationLink = activationUrl + "/api/v1.0/activate?token=" + savedProfile.getActivationToken();
            String subject = "MoneyManager | Profile Activation Email";
            String text = "Activate your profile by clicking the following link: " + activationLink;
            emailService.sendEmail(savedProfile.getEmail(), subject, text);
        }

        return modelMapper.map(savedProfile, ProfileDtoResponse.class);
    }

    public boolean activateAccount(String token){
        Optional<Profile> profileOptional = profileRepository.findByActivationToken(token);
        if(profileOptional.isEmpty()){
            throw new RuntimeException("Invalid activation token");
        }

        Profile profile = profileOptional.get();
        profile.setIsActive(true);
        profile.setActivationToken(null); // Clear the token after activation
        profileRepository.save(profile);
        return true;
    }

    public boolean isAccountActive(String email){
        return profileRepository.findByEmail(email)
                .map(Profile::getIsActive)
                .orElse(false);
    }

    public Profile getCurrentProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return profileRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
    }

    public ProfileDtoResponse getPublicProfile(String email){
        Profile currentProfile = null;

        if(email == null){
            currentProfile = getCurrentProfile();
        }else{
            currentProfile = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
        }

        return modelMapper.map(currentProfile, ProfileDtoResponse.class);
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDto authDto) {
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authDto.getEmail(),
                            authDto.getPassword()
                    )
            );
//            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtUtil.generateToken(authDto.getEmail());
            return Map.of(
                    "token", token,
                    "user", getPublicProfile(authDto.getEmail())
            );

        }catch (Exception e){
            throw new RuntimeException("Invalid email or password");
        }
    }
}
