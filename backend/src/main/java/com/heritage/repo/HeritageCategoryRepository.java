package com.heritage.repo;

import com.heritage.domain.HeritageCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeritageCategoryRepository extends JpaRepository<HeritageCategory, Long> {
}

