package com.heritage.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "page_banners")
public class PageBanner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String pageKey;  // 页面标识，如 projects, inheritors, activities 等

    private String title;      // 横幅标题
    private String subtitle;   // 副标题
    private String imageUrl;   // 背景图片URL
    private String status = "enabled";  // enabled/disabled
}
