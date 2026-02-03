package com.heritage.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "heritage_projects")
public class HeritageProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private Long categoryId;

    @Column(columnDefinition = "TEXT")
    private String intro;

    @Column(columnDefinition = "TEXT")
    private String history;

    @Column(columnDefinition = "TEXT")
    private String craftsmanship;

    @Column(columnDefinition = "TEXT")
    private String inheritance;

    @Column(columnDefinition = "TEXT")
    private String statusText;

    @Column
    private String coverImage;

    @Column
    private String location;

    @Column
    private String region;

    @Column
    private Double longitude;

    @Column
    private Double latitude;

    @Column
    private String tags;

    @Column
    private String publishStatus;

    @Column
    private Long createdBy;

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;
}

