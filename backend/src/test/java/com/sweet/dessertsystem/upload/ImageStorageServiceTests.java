package com.sweet.dessertsystem.upload;

import com.sweet.dessertsystem.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageStorageServiceTests {
    @TempDir Path tempDir;

    @Test
    void rejectsNonImage() {
        ImageStorageService service = new ImageStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file", "note.txt", "text/plain", "hello".getBytes());

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(BusinessException.class)
                .hasMessage("只允许上传 JPG、PNG、GIF 或 WEBP 图片");
    }

    @Test
    void storesImageWithGeneratedName() {
        ImageStorageService service = new ImageStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file", "cake.png", "image/png", new byte[]{1, 2, 3});

        String path = service.store(file);

        assertThat(path).matches("/uploads/desserts/[0-9a-f-]+\\.png");
    }
}
