package com.heritage.controller;

import com.heritage.domain.HeritageProject;
import com.heritage.domain.HeritageCategory;
import com.heritage.repo.HeritageProjectRepository;
import com.heritage.repo.HeritageCategoryRepository;
import com.heritage.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:5173")
public class ProjectMapController {
    
    @Autowired
    private HeritageProjectRepository heritageProjectRepository;

    @Autowired
    private HeritageCategoryRepository heritageCategoryRepository;
    
    /**
     * 获取项目地理位置数据（用于地图展示）
     */
    @GetMapping("/map")
    public ResponseEntity<?> getProjectsMap() {
        List<HeritageProject> projects = heritageProjectRepository.findAll();
        List<HeritageCategory> categories = heritageCategoryRepository.findAll();
        Map<Long, String> categoryMap = categories.stream()
            .collect(Collectors.toMap(HeritageCategory::getId, HeritageCategory::getName));
        
        // 转换为地图数据格式
        List<Map<String, Object>> mapData = projects.stream()
            .map(p -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", p.getId());
                item.put("name", p.getName());
                item.put("category", categoryMap.getOrDefault(p.getCategoryId(), "未分类"));
                item.put("description", p.getIntro());
                
                // 使用默认坐标（如果项目没有地理坐标，使用中国中心点）
                double longitude = p.getLongitude() != null ? p.getLongitude() : 104.0;
                double latitude = p.getLatitude() != null ? p.getLatitude() : 35.0;
                
                item.put("value", new double[]{longitude, latitude});
                item.put("longitude", longitude);
                item.put("latitude", latitude);
                
                return item;
            })
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", mapData);
        result.put("total", mapData.size());
        
        return ResponseEntity.ok(new ApiResponse(0, "success", result, Instant.now().toEpochMilli()));
    }
    
    /**
     * 按地区获取项目
     */
    @GetMapping("/by-region")
    public ResponseEntity<?> getProjectsByRegion(@RequestParam String region) {
        List<HeritageProject> projects = heritageProjectRepository.findAll();
        List<HeritageCategory> categories = heritageCategoryRepository.findAll();
        Map<Long, String> categoryMap = categories.stream()
            .collect(Collectors.toMap(HeritageCategory::getId, HeritageCategory::getName));
        
        List<Map<String, Object>> result = projects.stream()
            .filter(p -> p.getRegion() != null && p.getRegion().contains(region))
            .map(p -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", p.getId());
                item.put("name", p.getName());
                item.put("category", categoryMap.getOrDefault(p.getCategoryId(), "未分类"));
                item.put("region", p.getRegion());
                item.put("description", p.getIntro());
                return item;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse(0, "success", result, Instant.now().toEpochMilli()));
    }
    
    /**
     * 按分类获取项目
     */
    @GetMapping("/by-category")
    public ResponseEntity<?> getProjectsByCategory(@RequestParam String category) {
        List<HeritageCategory> categories = heritageCategoryRepository.findAll();
        Optional<HeritageCategory> cat = categories.stream()
            .filter(c -> c.getName().equals(category))
            .findFirst();
            
        if (!cat.isPresent()) {
            return ResponseEntity.ok(new ApiResponse(0, "success", new ArrayList<>(), Instant.now().toEpochMilli()));
        }
        
        Long targetCatId = cat.get().getId();
        List<HeritageProject> projects = heritageProjectRepository.findAll();
        
        List<Map<String, Object>> result = projects.stream()
            .filter(p -> p.getCategoryId() != null && p.getCategoryId().equals(targetCatId))
            .map(p -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", p.getId());
                item.put("name", p.getName());
                item.put("category", category);
                item.put("description", p.getIntro());
                return item;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse(0, "success", result, Instant.now().toEpochMilli()));
    }
    
    /**
     * 获取所有地区列表
     */
    @GetMapping("/regions")
    public ResponseEntity<?> getRegions() {
        List<HeritageProject> projects = heritageProjectRepository.findAll();
        
        List<String> regions = projects.stream()
            .map(HeritageProject::getRegion)
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse(0, "success", regions, Instant.now().toEpochMilli()));
    }
    
    /**
     * 获取所有分类列表
     */
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        List<HeritageCategory> categories = heritageCategoryRepository.findAll();
        
        List<String> categoryNames = categories.stream()
            .map(HeritageCategory::getName)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse(0, "success", categoryNames, Instant.now().toEpochMilli()));
    }
    
    /**
     * 获取地区统计数据
     */
    @GetMapping("/region-stats")
    public ResponseEntity<?> getRegionStats() {
        List<HeritageProject> projects = heritageProjectRepository.findAll();
        
        Map<String, Long> stats = projects.stream()
            .collect(Collectors.groupingBy(
                p -> p.getRegion() != null ? p.getRegion() : "未知",
                Collectors.counting()
            ));
        
        List<Map<String, Object>> result = new ArrayList<>();
        stats.forEach((region, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", region);
            item.put("value", count);
            result.add(item);
        });
        
        return ResponseEntity.ok(new ApiResponse(0, "success", result, Instant.now().toEpochMilli()));
    }
}
