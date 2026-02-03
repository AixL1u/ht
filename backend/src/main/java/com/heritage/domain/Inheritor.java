package com.heritage.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "inheritors")
public class Inheritor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String bio;

    @Column(length = 1000)
    private String skills;

    @Column
    private String avatarUrl;

    @Column
    private String contact;

    @Column
    private String region;

    @Column
    private String level;
}

