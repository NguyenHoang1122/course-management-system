package com.coursemanagementsystem.service;

import com.coursemanagementsystem.dto.UserDTO;
import com.coursemanagementsystem.model.Role;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.repository.RoleRepository;
import com.coursemanagementsystem.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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
        List<User> users = userRepository.findAll();
        List<User> result = new ArrayList<>();

        for (User user : users) {
            if (user.getRole() != null && user.getRole().getName().equals("INSTRUCTOR")) {
                result.add(user);
            }
        }

        return result;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElse(null);
    }

    public void register(String username, String password) {
        User user = new User();
        user.setUserName(username);
        user.setPassword(passwordEncoder.encode(password));

        // mặc định là STUDENT
        Role role = new Role();
        role.setName("STUDENT");
        user.setRole(role);

        userRepository.save(user);
    }

    public User findByUsername(String username) {
        Optional<User> user = userRepository.findByUserName(username);

        if (user.isPresent()) {
            return user.get();
        }

        return null;
    }

    public void updateProfile(String username, User userForm) {
        Optional<User> optionalUser = userRepository.findByUserName(username);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            user.setFullName(userForm.getFullName());
            user.setEmail(userForm.getEmail());

            userRepository.save(user);
        }
    }
}