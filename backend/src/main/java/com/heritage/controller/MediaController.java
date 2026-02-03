package com.heritage.controller;

import com.heritage.domain.MediaAsset;
import com.heritage.repo.MediaAssetRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.heritage.service.FfmpegService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MediaController {
    private final MediaAssetRepository mediaRepository;
    private final FfmpegService ffmpegService;

    @Value("${app.uploadDir:uploads}")
    private String uploadDir;

    public MediaController(MediaAssetRepository mediaRepository, FfmpegService ffmpegService) {
        this.mediaRepository = mediaRepository;
        this.ffmpegService = ffmpegService;
    }

    @GetMapping("/media/{id}")
    public MediaAsset get(@PathVariable Long id) {
        return mediaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("媒体资源不存在: " + id));
    }

    @PostMapping(value = "/admin/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public MediaAsset upload(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "title", required = false) String title,
                             @RequestParam(value = "projectId", required = false) Long projectId,
                             @RequestParam(value = "inheritorId", required = false) Long inheritorId,
                             @RequestParam(value = "type", required = false) String type
    ) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("文件为空");
        if (file.getSize() > 50L * 1024 * 1024) throw new IllegalArgumentException("文件过大");

        String contentType = file.getContentType();
        Set<String> allowed = Set.of("image/", "video/", "audio/", "application/pdf");
        boolean ok = false;
        if (contentType != null) {
            for (String p : allowed) {
                if (contentType.startsWith(p) || contentType.equals(p)) { ok = true; break; }
            }
        }
        if (!ok) throw new IllegalArgumentException("不支持的文件类型");

        Files.createDirectories(Paths.get(uploadDir));
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "");
        String saving = ext == null || ext.isBlank() ? filename : (filename + "." + ext);
        Path target = Paths.get(uploadDir, saving);
        file.transferTo(target);

        MediaAsset m = new MediaAsset();
        String t = type != null ? type : (contentType != null ? contentType.split("/")[0] : "file");
        m.setType(t);
        m.setUrl("/files/" + saving);
        m.setTitle(title);
        m.setProjectId(projectId);
        m.setInheritorId(inheritorId);
        try { m.setSize(Files.size(target)); } catch (Exception ignored) {}
        m.setStatus("video".equalsIgnoreCase(t) ? "queued" : "ready");
        m.setCreatedAt(Instant.now());
        m = mediaRepository.save(m);
        if ("video".equalsIgnoreCase(t)) {
            ffmpegService.transcodeToHlsAndCover(m.getId());
        }
        return m;
    }
}
