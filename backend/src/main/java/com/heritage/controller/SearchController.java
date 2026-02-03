package com.heritage.controller;

import com.heritage.service.SearchService;
import com.heritage.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    
    @Autowired
    private SearchService searchService;
    
    /**
     * 全文搜索
     */
    @GetMapping
    public ResponseEntity<?> search(@RequestParam("keyword") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(400, "Keyword cannot be empty", null, Instant.now().toEpochMilli()));
        }
        
        Map<String, Object> results = searchService.search(keyword.trim());
        
        Map<String, Object> response = new HashMap<>();
        response.put("keyword", keyword);
        response.put("results", results);
        
        return ResponseEntity.ok(new ApiResponse(0, "success", response, Instant.now().toEpochMilli()));
    }
    
    /**
     * 获取搜索建议
     */
    @GetMapping("/suggestions")
    public ResponseEntity<?> getSuggestions(@RequestParam(value = "prefix", required = false) String prefix) {
        List<String> suggestions = searchService.getSearchSuggestions(prefix);
        
        Map<String, Object> response = new HashMap<>();
        response.put("suggestions", suggestions);
        response.put("count", suggestions.size());
        
        return ResponseEntity.ok(new ApiResponse(0, "success", response, Instant.now().toEpochMilli()));
    }
    
    /**
     * 获取热门搜索词
     */
    @GetMapping("/hot")
    public ResponseEntity<?> getHotSearches() {
        List<String> hotSearches = searchService.getHotSearches();
        
        Map<String, Object> response = new HashMap<>();
        response.put("hotSearches", hotSearches);
        response.put("count", hotSearches.size());
        
        return ResponseEntity.ok(new ApiResponse(0, "success", response, Instant.now().toEpochMilli()));
    }

    /**
     * 同步数据到 Elasticsearch (仅管理员)
     */
    @PostMapping("/sync")
    public ResponseEntity<?> sync() {
        searchService.syncProjects();
        return ResponseEntity.ok(new ApiResponse(0, "同步任务已启动", null, Instant.now().toEpochMilli()));
    }
}
