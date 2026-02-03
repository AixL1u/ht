package com.heritage.repo;

import com.heritage.domain.Inheritor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InheritorRepository extends JpaRepository<Inheritor, Long> {
    Page<Inheritor> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}

