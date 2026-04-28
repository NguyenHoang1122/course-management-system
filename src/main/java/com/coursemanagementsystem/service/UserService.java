package com.coursemanagementsystem.service;

import com.coursemanagementsystem.dto.UserDTO;
import com.coursemanagementsystem.dto.UserProfileDTO;
import com.coursemanagementsystem.dto.UserRegisterDTO;
import com.coursemanagementsystem.model.Role;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.repository.course.CourseRepository;
import com.coursemanagementsystem.repository.EnrollmentRepository;
import com.coursemanagementsystem.repository.lesson.LessonProgressRepository;
import com.coursemanagementsystem.repository.RoleRepository;
import com.coursemanagementsystem.repository.review.ReviewRepository;
import com.coursemanagementsystem.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_INSTRUCTOR = "INSTRUCTOR";
    private static final String ROLE_PREFIX = "ROLE_";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private ReviewRepository reviewRepository;


    private UserDTO convertToDTO(User user) {
        UserDTO dto = modelMapper.map(user, UserDTO.class);

        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }

        return dto;
    }

    public UserDTO getProfile(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return convertToDTO(user);
    }

    public List<User> findAllInstructor() {
        return userRepository.findByRole_NameInAndDeletedFalseOrderByFullNameAsc(
                Arrays.asList(ROLE_INSTRUCTOR, ROLE_PREFIX + ROLE_INSTRUCTOR)
        );
    }

    public List<User> findAllActiveUsers() {
        return userRepository.findByDeletedFalseOrderByIdDesc();
    }

    public List<User> findAllDeletedUsers() {
        return userRepository.findByDeletedTrueOrderByDeletedAtDesc();
    }

    public List<User> findAllAdmins() {
        return userRepository.findByRole_NameInAndDeletedFalseOrderByFullNameAsc(
                Arrays.asList(ROLE_ADMIN, ROLE_PREFIX + ROLE_ADMIN)
        );
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

        Role role = roleRepository.findByName(ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Default role STUDENT not found"));
        user.setRole(role);
        user.setDeleted(false);
        user.setDeletedAt(null);

        userRepository.save(user);
    }

    public User findByUsername(String username) {
        Optional<User> user = userRepository.findByUserNameAndDeletedFalse(username);
        return user.orElse(null);
    }

    public void updateProfile(String username, UserProfileDTO profileDTO) {
        Optional<User> optionalUser = userRepository.findByUserNameAndDeletedFalse(username);

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
            user.setBio(profileDTO.getBio() == null ? null : profileDTO.getBio().trim());

            userRepository.save(user);
        }
    }

    public void changePassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findByUserNameAndDeletedFalse(username)
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

    @Transactional
    public void updateUserRole(Long userId, String roleName) {
        String normalizedRoleName = normalizeRoleName(roleName);
        if (!Arrays.asList(ROLE_STUDENT, ROLE_INSTRUCTOR).contains(normalizedRoleName)) {
            throw new IllegalArgumentException("Role is not supported");
        }

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (isAdminRole(user.getRole())) {
            throw new IllegalArgumentException("Cannot change role of admin account");
        }

        Role role = findRoleByNameNormalized(normalizedRoleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + normalizedRoleName));

        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void softDeleteUser(Long userId) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (isAdminRole(user.getRole())) {
            throw new IllegalArgumentException("Cannot delete admin account");
        }

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void restoreUser(Long userId) {
        User user = userRepository.findByIdAndDeletedTrue(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found in trash"));

        user.setDeleted(false);
        user.setDeletedAt(null);
        userRepository.save(user);
    }

    @Transactional
    public int purgeDeletedUsersOlderThanDays(int days) {
        LocalDateTime expiredAt = LocalDateTime.now().minusDays(days);
        List<User> expiredUsers = userRepository.findByDeletedTrueAndDeletedAtBefore(expiredAt);
        if (expiredUsers.isEmpty()) {
            return 0;
        }

        List<Long> userIds = expiredUsers.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        courseRepository.clearInstructorForUserIds(userIds);
        lessonProgressRepository.deleteByUserIds(userIds);
        reviewRepository.deleteByUserIds(userIds);
        enrollmentRepository.deleteByUserIds(userIds);
        userRepository.deleteAllByIdInBatch(userIds);

        return userIds.size();
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * *")
    public void purgeExpiredUsersFromTrash() {
        purgeDeletedUsersOlderThanDays(10);
    }

    private Optional<Role> findRoleByNameNormalized(String normalizedRoleName) {
        return roleRepository.findByName(normalizedRoleName)
                .or(() -> roleRepository.findByName(ROLE_PREFIX + normalizedRoleName));
    }

    private boolean isAdminRole(Role role) {
        return ROLE_ADMIN.equals(normalizeRoleName(role == null ? null : role.getName()));
    }

    private String normalizeRoleName(String roleName) {
        if (roleName == null) {
            return "";
        }

        String normalized = roleName.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith(ROLE_PREFIX)) {
            normalized = normalized.substring(ROLE_PREFIX.length());
        }
        return normalized;
    }
}