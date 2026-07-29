package com.sweet.dessertsystem.upload;

import com.sweet.dessertsystem.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageStorageService {
    private static final Map<String, String> ALLOWED = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/gif", "gif",
            "image/webp", "webp"
    );
    private final Path dessertDirectory;

    public ImageStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.dessertDirectory = Path.of(uploadDir).toAbsolutePath().normalize().resolve("desserts");
    }

    public String store(MultipartFile file) {
        String contentType = file == null ? null : file.getContentType();
        String extension = ALLOWED.get(contentType == null ? "" : contentType.toLowerCase(Locale.ROOT));
        String originalName = file == null ? "" : String.valueOf(file.getOriginalFilename());
        if (extension == null || !originalName.toLowerCase(Locale.ROOT).matches(".*\\.(jpg|jpeg|png|gif|webp)$")) {
            throw new BusinessException("只允许上传 JPG、PNG、GIF 或 WEBP 图片");
        }
        String filename = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(dessertDirectory);
            Files.copy(file.getInputStream(), dessertDirectory.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/desserts/" + filename;
        } catch (IOException exception) {
            throw new BusinessException("图片保存失败，请稍后重试");
        }
    }
}
