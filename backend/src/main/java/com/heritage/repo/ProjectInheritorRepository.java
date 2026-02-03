package com.heritage.repo;

import com.heritage.domain.ProjectInheritor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectInheritorRepository extends JpaRepository<ProjectInheritor, Long> {
    List<ProjectInheritor> findByProjectId(Long projectId);
    List<ProjectInheritor> findByInheritorId(Long inheritorId);
}

