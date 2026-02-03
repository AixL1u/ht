package com.heritage.repo;

import com.heritage.domain.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    List<MediaAsset> findByProjectId(Long projectId);
    List<MediaAsset> findByInheritorId(Long inheritorId);
    List<MediaAsset> findByType(String type);
    List<MediaAsset> findByTypeAndStatus(String type, String status);
    Page<MediaAsset> findByType(String type, Pageable pageable);
    Page<MediaAsset> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    Page<MediaAsset> findByTypeAndTitleContainingIgnoreCase(String type, String keyword, Pageable pageable);
}

