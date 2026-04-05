package com.pharma.service;

import com.pharma.entity.Document;
import com.pharma.entity.Tender;
import com.pharma.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@ApplicationScoped
public class FileUploadService {

    @ConfigProperty(name = "file.upload.directory")
    String uploadDirectory;

    @ConfigProperty(name = "file.upload.max-size", defaultValue = "10485760")
    Long maxFileSize;

    @Transactional
    public Document uploadDocument(
            FileUpload file, 
            Long tenderId, 
            String documentType, 
            String description,
            Long uploadedBy) throws IOException {
        
        // Validate file size
        if (file.size() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }

        // Validate file type
        String mimeType = file.contentType();
        if (!isAllowedMimeType(mimeType)) {
            throw new IllegalArgumentException("File type not allowed");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.fileName();
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueFilename = timestamp + "_" + UUID.randomUUID().toString() + extension;

        // Create tender subdirectory
        Path tenderPath = uploadPath.resolve("tender_" + tenderId);
        if (!Files.exists(tenderPath)) {
            Files.createDirectories(tenderPath);
        }

        // Save file
        Path filePath = tenderPath.resolve(uniqueFilename);
        Files.copy(file.uploadedFile().toPath(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create document record
        Document document = new Document();
        document.tender = Tender.findById(tenderId);
        document.uploadedBy = User.findById(uploadedBy);
        document.fileName = originalFilename;
        document.filePath = filePath.toString();
        document.fileSize = file.size();
        document.mimeType = mimeType;
        document.documentType = Document.DocumentType.valueOf(documentType);
        document.description = description;
        document.persist();

        return document;
    }

    public File getDocument(UUID uuid) throws IOException {
        Document document = Document.findByUuid(uuid);
        if (document == null) {
            throw new IllegalArgumentException("Document not found");
        }

        File file = new File(document.filePath);
        if (!file.exists()) {
            throw new IOException("File not found on disk");
        }

        return file;
    }

    @Transactional
    public void deleteDocument(UUID uuid) throws IOException {
        Document document = Document.findByUuid(uuid);
        if (document == null) {
            throw new IllegalArgumentException("Document not found");
        }

        // Delete file from disk
        Path filePath = Paths.get(document.filePath);
        Files.deleteIfExists(filePath);

        // Delete document record
        document.delete();
    }

    private boolean isAllowedMimeType(String mimeType) {
        return mimeType != null && (
            mimeType.equals("application/pdf") ||
            mimeType.equals("image/jpeg") ||
            mimeType.equals("image/png") ||
            mimeType.equals("application/msword") ||
            mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        );
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }
}
