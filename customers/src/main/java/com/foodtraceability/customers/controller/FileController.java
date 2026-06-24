package com.foodtraceability.customers.controller;

import com.foodtraceability.customers.dto.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileController {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail(400, "文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail(400, "只允许上传图片文件");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.fail(400, "图片大小不能超过10MB");
        }

        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + ext;

            Path dirPath = Paths.get(uploadDir, dateDir);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(newFileName);
            file.transferTo(filePath.toFile());

            String url = "/uploads/" + dateDir + "/" + newFileName;
            return Result.success(url);
        } catch (IOException e) {
            return Result.fail(500, "文件上传失败，请重试");
        }
    }
}
