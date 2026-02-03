package com.heritage.controller;

import com.heritage.config.JwtUtil;
import com.heritage.domain.User;
import com.heritage.domain.RolePermission;
import com.heritage.repo.UserRepository;
import com.heritage.repo.UserRoleRepository;
import com.heritage.repo.RolePermissionRepository;
import com.heritage.repo.PermissionRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import com.heritage.config.RateLimiterService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final RateLimiterService rateLimiterService;
    @Value("${app.ratelimit.login.limit:5}")
    private int loginLimit;
    @Value("${app.ratelimit.login.windowMs:60000}")
    private long loginWindowMs;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                          UserRoleRepository userRoleRepository, RolePermissionRepository rolePermissionRepository,
                          PermissionRepository permissionRepository, RateLimiterService rateLimiterService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
        this.rateLimiterService = rateLimiterService;
    }

    public static class LoginReq {
        @NotBlank public String username;
        @NotBlank public String password;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginReq req, HttpServletRequest httpReq) {
        String ip = httpReq.getRemoteAddr();
        String key = "login:" + req.username + ":" + ip;
        rateLimiterService.check(key, loginLimit, loginWindowMs);
        User u = userRepository.findByUsername(req.username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (!passwordEncoder.matches(req.password, u.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        java.util.Set<String> perms = new java.util.HashSet<>();
        for (var ur : userRoleRepository.findByUserId(u.getId())) {
            for (RolePermission rp : rolePermissionRepository.findByRoleId(ur.getRoleId())) {
                permissionRepository.findById(rp.getPermissionId())
                        .ifPresent(p -> perms.add(p.getCode()));
            }
        }
        String token = jwtUtil.generateToken(u.getUsername(), u.getRole(), new java.util.ArrayList<>(perms));
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("user", u);
        resp.put("perms", perms);
        return resp;
    }

    public static class RegisterReq {
        @NotBlank public String username;
        @NotBlank public String password;
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterReq req) {
        userRepository.findByUsername(req.username).ifPresent(x -> { throw new IllegalStateException("用户名已存在"); });
        User u = new User();
        u.setUsername(req.username);
        u.setPasswordHash(passwordEncoder.encode(req.password));
        u.setRole("USER");
        u.setStatus("ENABLED");
        u.setCreatedAt(Instant.now());
        return userRepository.save(u);
    }

    public static class RefreshReq {
        public String token;
    }

    @PostMapping("/refresh")
    public Map<String, String> refresh(@RequestHeader(value = "Authorization", required = false) String authorization,
                                       @RequestBody(required = false) RefreshReq req) {
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        } else if (req != null) {
            token = req.token;
        }
        if (token == null || !jwtUtil.validate(token)) {
            throw new IllegalArgumentException("无效的令牌");
        }
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);
        java.util.List<String> perms = jwtUtil.getPerms(token);
        String newToken = jwtUtil.generateToken(username, role, perms);
        return java.util.Map.of("token", newToken);
    }

    @PostMapping("/logout")
    public Map<String, String> logout() {
        // JWT 无状态，后端登出通常为前端删除 token 或放入黑名单（此处返回成功）
        return java.util.Map.of("message", "ok");
    }
}
