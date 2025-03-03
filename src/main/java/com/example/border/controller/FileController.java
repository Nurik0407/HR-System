package com.example.border.controller;

import com.example.border.service.impl.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "API для работы с файлами", description = "API для загрузки и скачивания файлов из MinIO")
public class FileController {

    private final MinioService minioService;

    public FileController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Загрузить файл",
            description = "Загружает файл в хранилище MinIO"
    )
    public ResponseEntity<String> uploadFile(
            @Parameter(required = true)
            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(minioService.uploadFile(file));
    }

    @GetMapping("/download/{objectName}")
    @Operation(
            summary = "Скачать файл",
            description = "Скачивает файл из хранилища MinIO по его имени"
    )
    public ResponseEntity<byte[]> downloadFile(@PathVariable String objectName) throws Exception {
        byte[] data = minioService.downloadFile(objectName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + objectName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @DeleteMapping("/{objectName}")
    @Operation(
            summary = "Удалить файл",
            description = "Удаляет файл из хранилища MinIO по имени"
    )
    public ResponseEntity<String> deleteFile(@PathVariable String objectName) throws Exception {
        return ResponseEntity.ok(minioService.deleteFile(objectName));
    }
}
