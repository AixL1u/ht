package com.heritage.service;

import com.heritage.domain.MediaAsset;
import com.heritage.repo.MediaAssetRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FfmpegService {
    private final MediaAssetRepository mediaRepository;

    @Value("${app.uploadDir:uploads}")
    private String uploadDir;

    @Value("${app.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    @Value("${app.ffmpeg.hlsTime:6}")
    private int hlsTime;

    public FfmpegService(MediaAssetRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    @Async
    public void transcodeToHlsAndCover(Long mediaId) {
        MediaAsset m = mediaRepository.findById(mediaId).orElse(null);
        if (m == null) return;
        try {
            m.setStatus("processing");
            mediaRepository.save(m);

            String url = m.getUrl(); // like /files/abc.mp4
            if (url == null || !url.startsWith("/files/")) {
                m.setStatus("failed");
                mediaRepository.save(m);
                return;
            }
            String fileName = url.substring("/files/".length());
            Path input = Paths.get(uploadDir, fileName);
            if (!Files.exists(input)) {
                m.setStatus("failed");
                mediaRepository.save(m);
                return;
            }
            String base = fileName;
            int dot = base.lastIndexOf('.');
            if (dot > 0) base = base.substring(0, dot);
            String outDirName = "hls/" + base + "-" + m.getId();
            Path outDir = Paths.get(uploadDir, outDirName);
            Files.createDirectories(outDir);

            Path m3u8 = outDir.resolve("master.m3u8");
            Path cover = outDir.resolve("cover.jpg");

            // ffmpeg HLS
            List<String> hlsCmd = new ArrayList<>();
            hlsCmd.add(ffmpegPath);
            hlsCmd.add("-y");
            hlsCmd.add("-i");
            hlsCmd.add(input.toAbsolutePath().toString());
            hlsCmd.add("-profile:v");
            hlsCmd.add("baseline");
            hlsCmd.add("-level");
            hlsCmd.add("3.0");
            hlsCmd.add("-start_number");
            hlsCmd.add("0");
            hlsCmd.add("-hls_time");
            hlsCmd.add(String.valueOf(hlsTime));
            hlsCmd.add("-hls_list_size");
            hlsCmd.add("0");
            hlsCmd.add("-f");
            hlsCmd.add("hls");
            hlsCmd.add(m3u8.toAbsolutePath().toString());

            runAndWait(hlsCmd);

            // cover
            List<String> coverCmd = new ArrayList<>();
            coverCmd.add(ffmpegPath);
            coverCmd.add("-y");
            coverCmd.add("-i");
            coverCmd.add(input.toAbsolutePath().toString());
            coverCmd.add("-ss");
            coverCmd.add("00:00:01");
            coverCmd.add("-vframes");
            coverCmd.add("1");
            coverCmd.add(cover.toAbsolutePath().toString());

            runAndWait(coverCmd);

            m.setHlsUrl("/files/" + outDirName + "/master.m3u8");
            m.setCoverUrl("/files/" + outDirName + "/cover.jpg");
            m.setStatus("ready");
            mediaRepository.save(m);
        } catch (Exception ex) {
            m.setStatus("failed");
            mediaRepository.save(m);
        }
    }

    private void runAndWait(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        p.getInputStream().transferTo(System.out);
        int code = p.waitFor();
        if (code != 0) throw new RuntimeException("ffmpeg exited with code " + code);
    }
}
