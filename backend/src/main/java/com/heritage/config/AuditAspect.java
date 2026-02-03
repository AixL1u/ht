package com.heritage.config;

import com.heritage.domain.AuditLog;
import com.heritage.repo.AuditLogRepository;
import com.heritage.repo.UserRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;

@Aspect
@Component
public class AuditAspect {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditAspect(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @AfterReturning("execution(* com.heritage.controller.AdminController.*(..))")
    public void afterAdminAction(JoinPoint jp) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (auth != null && auth.getName() != null) {
            userId = userRepository.findByUsername(auth.getName()).map(u -> u.getId()).orElse(null);
        }
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(jp.getSignature().getName());
        log.setModule("admin");
        String args = Arrays.toString(jp.getArgs());
        log.setDetail(args.length() > 1800 ? args.substring(0, 1800) + "..." : args);
        log.setCreatedAt(Instant.now());
        auditLogRepository.save(log);
    }
}
