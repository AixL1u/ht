package com.heritage.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column
    private String nickname;

    @Column(nullable = false)
    private String passwordHash;

    @Column
    private String role; // ADMIN/USER

    @Column
    private String email;

    @Column
    private String phone;

    @Column
    private String avatarUrl;

    @Column
    private String status; // ENABLED/DISABLED

    @Column
    private Instant createdAt;
}

