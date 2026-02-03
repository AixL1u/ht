package com.heritage.controller;

import com.heritage.domain.Activity;
import com.heritage.repo.ActivityRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {
    private final ActivityRepository activityRepository;

    public ActivityController(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @GetMapping
    public List<Activity> list() {
        return activityRepository.findAll();
    }

    @GetMapping("/{id}")
    public Activity getById(@PathVariable Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("活动不存在: " + id));
    }

    @GetMapping("/search")
    public Page<Activity> search(@RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 @RequestParam(value = "keyword", required = false) String keyword) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        if (keyword != null && !keyword.isBlank()) {
            return activityRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        }
        return activityRepository.findAll(pageable);
    }

    // ========== Admin APIs ==========

    @PostMapping("/admin")
    public Activity create(@RequestBody Activity activity) {
        activity.setId(null);
        return activityRepository.save(activity);
    }

    @PutMapping("/admin/{id}")
    public Activity update(@PathVariable Long id, @RequestBody Activity activity) {
        Activity existing = activityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("活动不存在: " + id));
        
        existing.setTitle(activity.getTitle());
        existing.setLocation(activity.getLocation());
        existing.setStartTime(activity.getStartTime());
        existing.setEndTime(activity.getEndTime());
        existing.setCapacity(activity.getCapacity());
        existing.setStatus(activity.getStatus());
        existing.setCover(activity.getCover());
        existing.setDescription(activity.getDescription());
        return activityRepository.save(existing);
    }

    @DeleteMapping("/admin/{id}")
    public void delete(@PathVariable Long id) {
        if (!activityRepository.existsById(id)) {
            throw new IllegalArgumentException("活动不存在: " + id);
        }
        activityRepository.deleteById(id);
    }
}

