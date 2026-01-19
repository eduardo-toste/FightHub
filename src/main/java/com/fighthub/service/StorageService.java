package com.fighthub.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${uploads.profile-dir}")
    private String profileDir;

    public String save(MultipartFile file, UUID userId) {
        try {
            Files.createDirectories(Path.of(profileDir));

            String ext = extension(file.getOriginalFilename());
            String key = userId + "-" + UUID.randomUUID() + ext;

            Path target = Path.of(profileDir).resolve(key).normalize();

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            return key;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar foto de perfil.", e);
        }
    }

    public void deleteIfExists(String key) {
        if (key == null || key.isBlank()) return;

        try {
            Path target = Path.of(profileDir).resolve(key).normalize();
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao remover foto de perfil.", e);
        }
    }

    private String extension(String filename) {
        if (filename == null) return "";
        int i = filename.lastIndexOf('.');
        return (i >= 0) ? filename.substring(i).toLowerCase() : "";
    }

}
