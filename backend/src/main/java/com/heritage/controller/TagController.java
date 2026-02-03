package com.heritage.controller;

import com.heritage.service.TagGeneratorService;
import com.heritage.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "http://localhost:5173")
public class TagController {
    
    @Autowired
    private TagGeneratorService tagGeneratorService;
    
    /**
     * 生成标签
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateTags(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        
        if (content == null || content.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(400, "Content cannot be empty", null, Instant.now().toEpochMilli()));
        }
        
        List<String> tags = tagGeneratorService.generateTags(content);
        
        Map<String, Object> result = new HashMap<>();
        result.put("tags", tags);
        result.put("count", tags.size());
        
        return ResponseEntity.ok(new ApiResponse(0, "success", result, Instant.now().toEpochMilli()));
    }
    
    /**
     * 提取关键词
     */
    @PostMapping("/extract")
    public ResponseEntity<?> extractKeywords(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        
        if (content == null || content.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(400, "Content cannot be empty", null, Instant.now().toEpochMilli()));
        }
        
        List<String> keywords = tagGeneratorService.extractKeywords(content);
        
        Map<String, Object> result = new HashMap<>();
        result.put("keywords", keywords);
        result.put("count", keywords.size());
        
        return ResponseEntity.ok(new ApiResponse(0, "success", result, Instant.now().toEpochMilli()));
    }
    
    /**
     * 获取标签建议
     */
    @GetMapping("/suggestions")
    public ResponseEntity<?> getTagSuggestions(@RequestParam(required = false) String prefix) {
        List<String> suggestions = tagGeneratorService.getTagSuggestions(prefix);
        
        Map<String, Object> result = new HashMap<>();
        result.put("suggestions", suggestions);
        result.put("count", suggestions.size());
        
        return ResponseEntity.ok(new ApiResponse(0, "success", result, Instant.now().toEpochMilli()));
    }
    
    /**
     * 获取所有可用标签
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllTags() {
        List<String> tags = tagGeneratorService.getAllTags();
        
        Map<String, Object> result = new HashMap<>();
        result.put("tags", tags);
        result.put("count", tags.size());
        
        return ResponseEntity.ok(new ApiResponse(0, "success", result, Instant.now().toEpochMilli()));
    }
}
