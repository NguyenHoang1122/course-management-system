package com.coursemanagementsystem.service;

import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> findAllInstructor(){
        return userRepository.findAll();
    }

    public void register(String username, String password) {
    }
}
