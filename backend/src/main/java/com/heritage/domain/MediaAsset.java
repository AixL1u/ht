package com.heritage.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "media_assets")
public class MediaAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String url;

    @Column
    private String title;

    @Column
    private Long projectId;

    @Column
    private Long inheritorId;

    @Column
    private Integer duration;

    @Column
    private Long size;

    @Column(length = 4000)
    private String transcript;

    @Column
    private String hlsUrl;

    @Column
    private String coverUrl;

    @Column
    private String status;

    @Column
    private String category;

    @Column
    private String tags;

    @Column
    private Integer viewCount = 0;

    @Column
    private Instant createdAt;
}

