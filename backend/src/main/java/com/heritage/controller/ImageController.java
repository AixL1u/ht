package com.heritage.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 通用图片上传控制器
 * 支持上传项目、商品、活动等图片
 */
@RestController
@RequestMapping("/api")
public class ImageController {

    @Value("${app.uploadDir:uploads}")
    private String uploadDir;

    // 允许的图片类型
    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    // 允许的子目录
    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
        "projects", "products", "activities", "banners", "avatars", "common"
    );

    /**
     * 上传图片
     * @param file 图片文件
     * @param category 分类目录 (projects/products/activities/banners/common)
     * @return 图片访问URL
     */
    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "common") String category) throws IOException {
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("不支持的图片格式，仅支持 JPG/PNG/GIF/WEBP");
        }

        // 验证分类目录
        if (!ALLOWED_CATEGORIES.contains(category.toLowerCase())) {
            category = "common";
        }

        // 创建目录
        Path categoryDir = Paths.get(uploadDir, "images", category);
        Files.createDirectories(categoryDir);

        // 生成文件名
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "");
        String saving = ext == null || ext.isBlank() ? filename + ".jpg" : (filename + "." + ext);
        
        // 保存文件
        Path target = categoryDir.resolve(saving);
        file.transferTo(target);

        // 返回访问URL
        String url = "/files/images/" + category + "/" + saving;
        
        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        result.put("filename", saving);
        result.put("category", category);
        result.put("size", file.getSize());
        
        return result;
    }

    /**
     * 公开上传接口（用于用户头像等）
     */
    @PostMapping(value = "/public/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadPublicImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("不支持的图片格式");
        }

        // 限制文件大小 2MB
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("图片大小不能超过2MB");
        }

        Path categoryDir = Paths.get(uploadDir, "images", "avatars");
        Files.createDirectories(categoryDir);

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "");
        String saving = ext == null || ext.isBlank() ? filename + ".jpg" : (filename + "." + ext);
        
        Path target = categoryDir.resolve(saving);
        file.transferTo(target);

        String url = "/files/images/avatars/" + saving;
        
        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        
        return result;
    }
}
