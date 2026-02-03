package com.heritage.controller;

import com.heritage.domain.PageBanner;
import com.heritage.repository.PageBannerRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PageBannerController {

    private final PageBannerRepository pageBannerRepository;

    public PageBannerController(PageBannerRepository pageBannerRepository) {
        this.pageBannerRepository = pageBannerRepository;
    }

    // 公开接口：获取指定页面的横幅配置
    @GetMapping("/page-banners/{pageKey}")
    public PageBanner getBanner(@PathVariable String pageKey) {
        return pageBannerRepository.findByPageKey(pageKey)
            .orElseThrow(() -> new IllegalArgumentException("页面横幅不存在: " + pageKey));
    }

    // 公开接口：获取所有横幅配置
    @GetMapping("/page-banners")
    public List<PageBanner> getAllBanners() {
        return pageBannerRepository.findAll();
    }

    // 管理接口：更新或创建横幅配置
    @PostMapping("/admin/page-banners")
    @PreAuthorize("hasRole('ADMIN')")
    public PageBanner saveBanner(@RequestBody PageBanner banner) {
        if (banner.getPageKey() == null || banner.getPageKey().isBlank()) {
            throw new IllegalArgumentException("pageKey 不能为空");
        }
        
        // 查找是否已存在
        PageBanner existing = pageBannerRepository.findByPageKey(banner.getPageKey()).orElse(null);
        if (existing != null) {
            existing.setTitle(banner.getTitle());
            existing.setSubtitle(banner.getSubtitle());
            existing.setImageUrl(banner.getImageUrl());
            existing.setStatus(banner.getStatus() != null ? banner.getStatus() : "enabled");
            return pageBannerRepository.save(existing);
        }
        
        return pageBannerRepository.save(banner);
    }

    // 管理接口：删除横幅配置
    @DeleteMapping("/admin/page-banners/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBanner(@PathVariable Long id) {
        pageBannerRepository.deleteById(id);
    }
}
