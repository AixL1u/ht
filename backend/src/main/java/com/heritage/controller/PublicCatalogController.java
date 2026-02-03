package com.heritage.controller;

import com.heritage.domain.*;
import com.heritage.repo.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/catalog")
public class PublicCatalogController {
    private final HeritageCategoryRepository categoryRepository;
    private final HeritageProjectRepository projectRepository;
    private final InheritorRepository inheritorRepository;
    private final ProjectInheritorRepository projectInheritorRepository;
    private final MediaAssetRepository mediaRepository;

    public PublicCatalogController(HeritageCategoryRepository categoryRepository, HeritageProjectRepository projectRepository, InheritorRepository inheritorRepository, ProjectInheritorRepository projectInheritorRepository, MediaAssetRepository mediaRepository) {
        this.categoryRepository = categoryRepository;
        this.projectRepository = projectRepository;
        this.inheritorRepository = inheritorRepository;
        this.projectInheritorRepository = projectInheritorRepository;
        this.mediaRepository = mediaRepository;
    }

    @GetMapping("/categories")
    public List<HeritageCategory> categories() { return categoryRepository.findAll(); }

    @GetMapping("/projects")
    public List<HeritageProject> projects() { return projectRepository.findAll(); }

    @GetMapping("/projects/search")
    public Page<HeritageProject> searchProjects(@RequestParam(value = "page", defaultValue = "0") int page,
                                                @RequestParam(value = "size", defaultValue = "10") int size,
                                                @RequestParam(value = "keyword", required = false) String keyword,
                                                @RequestParam(value = "categoryId", required = false) Long categoryId) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        if (categoryId != null && keyword != null && !keyword.isBlank()) {
            return projectRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, keyword, pageable);
        } else if (categoryId != null) {
            return projectRepository.findByCategoryId(categoryId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            return projectRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            return projectRepository.findAll(pageable);
        }
    }

    @GetMapping("/projects/{id}")
    public HeritageProject project(@PathVariable Long id) { 
        return projectRepository.findById(id).orElseThrow(() -> 
            new IllegalArgumentException("项目不存在: " + id)
        ); 
    }

    @GetMapping("/projects/{id}/related")
    public List<HeritageProject> getRelatedProjects(@PathVariable Long id) {
        HeritageProject current = projectRepository.findById(id).orElseThrow();
        List<HeritageProject> all = projectRepository.findAll();
        
        // 随机推荐算法：每次都随机返回不同的项目
        List<HeritageProject> candidates = all.stream()
            .filter(p -> !p.getId().equals(id))
            .collect(Collectors.toList());
        
        // 打乱顺序
        Collections.shuffle(candidates);
        
        // 返回前4个
        return candidates.stream()
            .limit(4)
            .collect(Collectors.toList());
    }

    @GetMapping("/projects/{id}/inheritors")
    public List<ProjectInheritor> projectInheritors(@PathVariable Long id) { return projectInheritorRepository.findByProjectId(id); }

    @GetMapping("/inheritors")
    public List<Inheritor> inheritors() { return inheritorRepository.findAll(); }

    @GetMapping("/inheritors/{id}")
    public Inheritor inheritor(@PathVariable Long id) { 
        return inheritorRepository.findById(id).orElseThrow(() -> 
            new IllegalArgumentException("传承人不存在: " + id)
        ); 
    }

    @GetMapping("/media/project/{projectId}")
    public List<MediaAsset> mediaByProject(@PathVariable Long projectId) { return mediaRepository.findByProjectId(projectId); }

    @GetMapping("/media/inheritor/{inheritorId}")
    public List<MediaAsset> mediaByInheritor(@PathVariable Long inheritorId) { return mediaRepository.findByInheritorId(inheritorId); }

    @GetMapping("/projects/map")
    public List<HeritageProject> projectsMap() { return projectRepository.findAll(); }

    @GetMapping("/projects/regions")
    public List<String> projectRegions() {
        return projectRepository.findAll().stream()
            .map(HeritageProject::getRegion)
            .filter(r -> r != null && !r.isBlank())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    @GetMapping("/projects/categories")
    public List<String> projectCategories() {
        return categoryRepository.findAll().stream()
            .map(HeritageCategory::getName)
            .collect(Collectors.toList());
    }
}

