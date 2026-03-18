package org.hartford.binsure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hartford.binsure.entity.Document;
import org.hartford.binsure.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Document Management", description = "Endpoints for uploading and downloading supporting documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a document", description = "Uploads a multipart file linked to an application or claim")
    public ResponseEntity<Document> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "applicationId", required = false) Long applicationId,
            @RequestParam(value = "claimId", required = false) Long claimId,
            Authentication authentication) throws IOException {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.uploadDocument(file, documentType, applicationId, claimId, email));
    }

    @GetMapping("/application/{id}")
    @Operation(summary = "List documents for application")
    public List<Document> getByApplication(@PathVariable("id") Long id) {
        return documentService.getByApplication(id);
    }

    @GetMapping("/claim/{id}")
    @Operation(summary = "List documents for claim")
    public List<Document> getByClaim(@PathVariable("id") Long id) {
        return documentService.getByClaim(id);
    }

    @GetMapping("/view/{id}")
    @Operation(summary = "View/Download a document")
    public ResponseEntity<Resource> view(@PathVariable("id") Long id) throws IOException {
        Document doc = documentService.getById(id);
        Path path = documentService.getFilePath(doc);
        Resource resource = new UrlResource(path.toUri());

        if (resource.exists() || resource.isReadable()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(doc.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                    .body(resource);
        } else {
            throw new RuntimeException("Could not read the file!");
        }
    }
}
