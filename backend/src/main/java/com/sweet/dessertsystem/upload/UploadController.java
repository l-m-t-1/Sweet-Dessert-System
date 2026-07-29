package com.sweet.dessertsystem.upload;

import com.sweet.dessertsystem.common.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/upload")
public class UploadController {
    private final ImageStorageService storageService;

    public UploadController(ImageStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/dessert")
    public ApiResponse<Map<String, String>> upload(@RequestParam MultipartFile file) {
        return ApiResponse.ok(Map.of("path", storageService.store(file)));
    }
}
