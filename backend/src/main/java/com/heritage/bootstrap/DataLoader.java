package com.heritage.bootstrap;

import com.heritage.domain.User;
import com.heritage.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 只创建用户账号（密码需要BCrypt加密）
 * 其他数据通过SQL文件手动导入
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 管理员账号: god / root
        createUserIfNotExists("god", "root", "ADMIN", "god@heritage.com", "13800000001");
        
        // 普通用户: Liu / 0914
        createUserIfNotExists("Liu", "0914", "USER", "liu@heritage.com", "13800000002");
        
        System.out.println("✅ 用户账号初始化完成");
        System.out.println("   管理员: god / root");
        System.out.println("   用户: Liu / 0914");
    }

    private void createUserIfNotExists(String username, String password, String role, String email, String phone) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(role);
            user.setEmail(email);
            user.setPhone(phone);
            user.setStatus("ENABLED");
            user.setCreatedAt(Instant.now());
            userRepository.save(user);
            System.out.println("  ✓ 创建用户: " + username + " (" + role + ")");
        }
    }
}
