package com.coursemanagementsystem.service;

import com.coursemanagementsystem.dto.UserDTO;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;

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

    public User findById(Long id) {
        return null;
    }

    public void register(String username, String password) {
    }
}
