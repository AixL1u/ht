package com.heritage.repository;

import com.heritage.domain.PageBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PageBannerRepository extends JpaRepository<PageBanner, Long> {
    Optional<PageBanner> findByPageKey(String pageKey);
}
