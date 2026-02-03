package com.heritage.repo;

import com.heritage.domain.HeritageProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HeritageProjectRepository extends JpaRepository<HeritageProject, Long> {
    Page<HeritageProject> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    Page<HeritageProject> findByCategoryId(Long categoryId, Pageable pageable);
    Page<HeritageProject> findByCategoryIdAndNameContainingIgnoreCase(Long categoryId, String keyword, Pageable pageable);
}

