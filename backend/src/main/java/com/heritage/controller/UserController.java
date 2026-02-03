package com.heritage.controller;

import com.heritage.domain.User;
import com.heritage.repo.UserRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.uploadDir:uploads}")
    private String uploadDir;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/{id}")
    public User get(@PathVariable Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + id));
    }

    public static class UpdateProfileReq {
        @NotNull public String email;
        @NotNull public String phone;
        public String avatarUrl;
        public String nickname;
    }

    @PutMapping("/{id}")
    public User updateProfile(@PathVariable Long id, @RequestBody UpdateProfileReq req) {
        User u = userRepository.findById(id).orElseThrow();
        u.setEmail(req.email);
        u.setPhone(req.phone);
        u.setAvatarUrl(req.avatarUrl);
        u.setNickname(req.nickname);
        return userRepository.save(u);
    }

    @PostMapping("/me/avatar")
    public Map<String, String> uploadAvatar(@RequestParam("file") MultipartFile file) throws java.io.IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("文件为空");
        
        Files.createDirectories(Paths.get(uploadDir));
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "");
        String saving = ext == null || ext.isBlank() ? filename : (filename + "." + ext);
        Path target = Paths.get(uploadDir, saving);
        file.transferTo(target);
        
        String url = "/files/" + saving;
        Map<String, String> res = new HashMap<>();
        res.put("url", url);
        return res;
    }

    public static class UpdatePasswordReq {
        @NotBlank public String newPassword;
    }

    @PutMapping("/{id}/password")
    public User updatePassword(@PathVariable Long id, @RequestBody UpdatePasswordReq req) {
        User u = userRepository.findById(id).orElseThrow();
        u.setPasswordHash(passwordEncoder.encode(req.newPassword));
        return userRepository.save(u);
    }

    @GetMapping("/me")
    public User me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new java.util.NoSuchElementException();
        String username = auth.getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    @PutMapping("/me")
    public User updateMe(@RequestBody UpdateProfileReq req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new java.util.NoSuchElementException();
        String username = auth.getName();
        User u = userRepository.findByUsername(username).orElseThrow();
        u.setEmail(req.email);
        u.setPhone(req.phone);
        u.setAvatarUrl(req.avatarUrl);
        u.setNickname(req.nickname);
        return userRepository.save(u);
    }

    @PutMapping("/me/password")
    public User updateMyPassword(@RequestBody UpdatePasswordReq req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new java.util.NoSuchElementException();
        String username = auth.getName();
        User u = userRepository.findByUsername(username).orElseThrow();
        u.setPasswordHash(passwordEncoder.encode(req.newPassword));
        return userRepository.save(u);
    }
}

