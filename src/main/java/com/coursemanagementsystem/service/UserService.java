package com.coursemanagementsystem.service;

import com.coursemanagementsystem.dto.UserDTO;
import com.coursemanagementsystem.dto.UserProfileDTO;
import com.coursemanagementsystem.dto.UserRegisterDTO;
import com.coursemanagementsystem.model.Role;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.repository.RoleRepository;
import com.coursemanagementsystem.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;


    private UserDTO convertToDTO(User user) {
        UserDTO dto = modelMapper.map(user, UserDTO.class);

        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }

        return dto;
    }

    public UserDTO getProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return convertToDTO(user);
    }

    public List<User> findAllInstructor() {
        return userRepository.findAll();
        }

    public UserDTO save(UserDTO userDTO) {
        User user = modelMapper.map(userDTO, User.class);
        User savedUser = userRepository.save(user);

        return modelMapper.map(savedUser, UserDTO.class);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void register(UserRegisterDTO dto) {
        if (userRepository.findByUserName(dto.getUserName()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }

        User user = new User();
        user.setUserName(dto.getUserName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());

        Role role = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("Default role STUDENT not found"));
        user.setRole(role);

        userRepository.save(user);
    }

    public User findByUsername(String username) {
        Optional<User> user = userRepository.findByUserName(username);
        return user.orElse(null);
    }

    public void updateProfile(String username, UserProfileDTO profileDTO) {
        Optional<User> optionalUser = userRepository.findByUserName(username);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            String email = profileDTO.getEmail() == null ? "" : profileDTO.getEmail().trim();
            if (!email.isEmpty() && userRepository.existsByEmailAndUserNameNot(email, username)) {
                throw new IllegalArgumentException("Email already exists");
            }

            user.setFullName(profileDTO.getFullName() == null ? null : profileDTO.getFullName().trim());
            user.setEmail(email);
            user.setAvatar(profileDTO.getAvatar());
            user.setPhone(profileDTO.getPhone() == null ? null : profileDTO.getPhone().trim());
            user.setAddress(profileDTO.getAddress() == null ? null : profileDTO.getAddress().trim());

            userRepository.save(user);
        }
    }

    public void changePassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password is required");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}