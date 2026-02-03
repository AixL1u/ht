package com.heritage.controller;

import com.heritage.domain.*;
import com.heritage.repo.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PublicInfoController {
    private final NewsRepository newsRepository;
    private final VideoRepository videoRepository;
    private final MediaAssetRepository mediaAssetRepository;

    public PublicInfoController(NewsRepository newsRepository, VideoRepository videoRepository, MediaAssetRepository mediaAssetRepository) {
        this.newsRepository = newsRepository;
        this.videoRepository = videoRepository;
        this.mediaAssetRepository = mediaAssetRepository;
    }

    @GetMapping("/news")
    public List<News> newsList() { return newsRepository.findAll(); }

    @GetMapping("/news/{id}")
    public News getNews(@PathVariable Long id) {
        return newsRepository.findById(id).orElse(null);
    }

    @GetMapping("/news/search")
    public Page<News> searchNews(@RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 @RequestParam(value = "type", required = false) String type,
                                 @RequestParam(value = "keyword", required = false) String keyword) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        if (type != null && !type.isBlank() && keyword != null && !keyword.isBlank()) {
            return newsRepository.findByTypeAndTitleContainingIgnoreCase(type, keyword, pageable);
        } else if (type != null && !type.isBlank()) {
            return newsRepository.findByType(type, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            return newsRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else {
            return newsRepository.findAll(pageable);
        }
    }

    @GetMapping("/videos")
    public List<Video> videoList() { return videoRepository.findAll(); }

    @GetMapping("/videos/search")
    public Page<Video> searchVideos(@RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                    @RequestParam(value = "keyword", required = false) String keyword) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        if (keyword != null && !keyword.isBlank()) {
            return videoRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        }
        return videoRepository.findAll(pageable);
    }

    @GetMapping("/media/videos")
    public List<MediaAsset> mediaVideosReady() {
        return mediaAssetRepository.findByType("video");
    }

    @PostMapping("/media/videos/{id}/view")
    public MediaAsset incrementViewCount(@PathVariable Long id) {
        MediaAsset media = mediaAssetRepository.findById(id).orElse(null);
        if (media != null) {
            media.setViewCount((media.getViewCount() == null ? 0 : media.getViewCount()) + 1);
            mediaAssetRepository.save(media);
        }
        return media;
    }
}

