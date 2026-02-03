package com.heritage.controller;

import com.heritage.domain.MediaAsset;
import com.heritage.repo.MediaAssetRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import com.heritage.service.FfmpegService;

@RestController
@RequestMapping("/api/admin/media/chunk")
public class MediaChunkController {
    private final MediaAssetRepository mediaRepository;
    private final FfmpegService ffmpegService;

    @Value("${app.uploadDir:uploads}")
    private String uploadDir;

    public MediaChunkController(MediaAssetRepository mediaRepository, FfmpegService ffmpegService) {
        this.mediaRepository = mediaRepository;
        this.ffmpegService = ffmpegService;
    }

    public static class InitReq {
        public String filename;
        public Integer totalChunks;
    }

    public static class MergeReq {
        public String uploadId;
        public String filename;
        public String type;
        public String title;
        public String category;
        public String tags;
        public String coverUrl;
        public Long projectId;
        public Long inheritorId;
    }

    @PostMapping("/initiate")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> initiate(@RequestBody InitReq req) throws IOException {
        if (req.filename == null || req.filename.isBlank()) throw new IllegalArgumentException("文件名必填");
        String uploadId = UUID.randomUUID().toString().replace("-", "");
        Path tmp = Paths.get(uploadDir, "tmp", uploadId);
        Files.createDirectories(tmp);
        return Map.of("uploadId", uploadId);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> upload(@RequestParam("uploadId") String uploadId,
                                      @RequestParam("index") Integer index,
                                      @RequestParam("chunk") MultipartFile chunk) throws IOException {
        if (chunk.isEmpty()) throw new IllegalArgumentException("分片为空");
        Path dir = Paths.get(uploadDir, "tmp", uploadId);
        if (!Files.exists(dir)) throw new IllegalArgumentException("uploadId 不存在");
        Path target = dir.resolve(index + ".part");
        chunk.transferTo(target);
        return Map.of("ok", true);
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> status(@RequestParam("uploadId") String uploadId) throws IOException {
        Path dir = Paths.get(uploadDir, "tmp", uploadId);
        if (!Files.exists(dir)) return Map.of("exists", false, "uploaded", List.of());
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.part")) {
            List<Integer> uploaded = new ArrayList<>();
            for (Path p : ds) {
                String name = p.getFileName().toString();
                String idx = name.substring(0, name.indexOf('.'));
                uploaded.add(Integer.parseInt(idx));
            }
            Collections.sort(uploaded);
            return Map.of("exists", true, "uploaded", uploaded);
        }
    }

    @PostMapping("/merge")
    @PreAuthorize("hasRole('ADMIN')")
    public MediaAsset merge(@RequestBody MergeReq req) throws IOException {
        if (req.uploadId == null || req.uploadId.isBlank()) throw new IllegalArgumentException("uploadId 必填");
        if (req.filename == null || req.filename.isBlank()) throw new IllegalArgumentException("filename 必填");
        Path dir = Paths.get(uploadDir, "tmp", req.uploadId);
        if (!Files.exists(dir)) throw new IllegalArgumentException("uploadId 不存在");

        Files.createDirectories(Paths.get(uploadDir));
        String ext = StringUtils.getFilenameExtension(req.filename);
        String name = UUID.randomUUID().toString().replace("-", "");
        String saving = (ext == null || ext.isBlank()) ? name : (name + "." + ext);
        Path output = Paths.get(uploadDir, saving);

        // 合并
        List<Path> parts = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.part")) {
            for (Path p : ds) parts.add(p);
        }
        parts.sort(Comparator.comparingInt(p -> Integer.parseInt(p.getFileName().toString().split("\\.")[0])));
        try (OutputStream os = Files.newOutputStream(output, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Path p : parts) {
                Files.copy(p, os);
            }
        }
        // 清理临时分片
        for (Path p : parts) Files.deleteIfExists(p);
        Files.deleteIfExists(dir);

        // 保存媒体记录
        MediaAsset m = new MediaAsset();
        String type = req.type != null ? req.type : "file";
        m.setType(type);
        m.setUrl("/files/" + saving);
        m.setTitle(req.title);
        m.setCategory(req.category);
        m.setTags(req.tags);
        m.setCoverUrl(req.coverUrl);
        m.setProjectId(req.projectId);
        m.setInheritorId(req.inheritorId);
        try { m.setSize(Files.size(output)); } catch (Exception ignored) {}
        m.setStatus("video".equalsIgnoreCase(type) ? "queued" : "ready");
        m.setCreatedAt(Instant.now());
        m = mediaRepository.save(m);

        // 如果没有手动上传封面，才自动截取
        if ("video".equalsIgnoreCase(type) && (req.coverUrl == null || req.coverUrl.isBlank())) {
            ffmpegService.transcodeToHlsAndCover(m.getId());
        } else if ("video".equalsIgnoreCase(type)) {
            // 有封面但仍需转码HLS
            m.setStatus("ready");
            mediaRepository.save(m);
        }
        return m;
    }
}
