package com.heritage.repo;

import com.heritage.domain.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewsRepository extends JpaRepository<News, Long> {
    Page<News> findByType(String type, Pageable pageable);
    Page<News> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    Page<News> findByTypeAndTitleContainingIgnoreCase(String type, String keyword, Pageable pageable);
}

