package com.heritage.controller;

import com.heritage.domain.Banner;
import com.heritage.domain.SiteSetting;
import com.heritage.repo.BannerRepository;
import com.heritage.repo.SiteSettingRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PublicSettingsController {
    private final BannerRepository bannerRepository;
    private final SiteSettingRepository siteSettingRepository;

    public PublicSettingsController(BannerRepository bannerRepository, SiteSettingRepository siteSettingRepository) {
        this.bannerRepository = bannerRepository;
        this.siteSettingRepository = siteSettingRepository;
    }

    @GetMapping("/banners")
    public List<Banner> banners() { return bannerRepository.findAll(); }

    @GetMapping("/settings")
    public List<SiteSetting> settings() { return siteSettingRepository.findAll(); }
}

