package org.hartford.binsure.service;

import org.hartford.binsure.entity.Claim;
import org.hartford.binsure.entity.Document;
import org.hartford.binsure.entity.PolicyApplication;
import org.hartford.binsure.entity.User;
import org.hartford.binsure.enums.DocumentType;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.repository.ClaimRepository;
import org.hartford.binsure.repository.DocumentRepository;
import org.hartford.binsure.repository.PolicyApplicationRepository;
import org.hartford.binsure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PolicyApplicationRepository applicationRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private UserRepository userRepository;

    private final Path root = Paths.get("uploads");

    public DocumentService() {
        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    public Document uploadDocument(MultipartFile file, String docType, Long applicationId, Long claimId,
            String userEmail) throws IOException {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // Generate unique filename to avoid collisions
        String storedFileName = UUID.randomUUID().toString() + extension;
        Files.copy(file.getInputStream(), this.root.resolve(storedFileName), StandardCopyOption.REPLACE_EXISTING);

        Document doc = Document.builder()
                .fileName(originalFileName)
                .fileType(file.getContentType())
                .filePath(storedFileName) // Store only the filename/relative path
                .documentType(DocumentType.valueOf(docType))
                .uploadedBy(user)
                .build();

        if (applicationId != null) {
            PolicyApplication app = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));
            doc.setApplication(app);
        }

        if (claimId != null) {
            Claim claim = claimRepository.findById(claimId)
                    .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", claimId));
            doc.setClaim(claim);
        }

        return documentRepository.save(doc);
    }

    public List<Document> getByApplication(Long appId) {
        return documentRepository.findByApplicationId(appId);
    }

    public List<Document> getByClaim(Long claimId) {
        return documentRepository.findByClaimId(claimId);
    }

    public Document getById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
    }

    public Path getFilePath(Document doc) {
        return root.resolve(doc.getFilePath());
    }
}
