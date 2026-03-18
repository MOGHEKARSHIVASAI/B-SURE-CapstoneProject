package org.hartford.binsure.service;

import org.hartford.binsure.entity.Document;
import org.hartford.binsure.entity.PolicyApplication;
import org.hartford.binsure.entity.Claim;
import org.hartford.binsure.entity.User;
import org.hartford.binsure.enums.DocumentType;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.repository.ClaimRepository;
import org.hartford.binsure.repository.DocumentRepository;
import org.hartford.binsure.repository.PolicyApplicationRepository;
import org.hartford.binsure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DocumentService.
 * Tests document upload, retrieval, and file operations.
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private PolicyApplicationRepository applicationRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private DocumentService documentService;

    private User user;
    private Document document;
    private PolicyApplication application;
    private Claim claim;

    @BeforeEach
    void setUp() {
        // Setup user
        user = new User();
        user.setId(1L);
        user.setEmail("customer@example.com");

        // Setup application
        application = new PolicyApplication();
        application.setId(1L);

        // Setup claim
        claim = new Claim();
        claim.setId(1L);

        // Setup document
        document = new Document();
        document.setId(1L);
        document.setFileName("policy_doc.pdf");
        document.setFileType("application/pdf");
        document.setFilePath("uploads/unique-uuid.pdf");
        document.setDocumentType(DocumentType.POLICY_DOCUMENT);
        document.setUploadedBy(user);
        document.setApplication(application);
    }

    @Test
    void testUploadDocument_WithApplication_Success() throws IOException {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("policy.pdf");
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(new byte[0]));
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(documentRepository.save(any(Document.class))).thenReturn(document);

        // Act
        Document result = documentService.uploadDocument(
                multipartFile, "POLICY_DOCUMENT", 1L, null, "customer@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("policy.pdf", result.getFileName());
        assertEquals(DocumentType.POLICY_DOCUMENT, result.getDocumentType());
        verify(userRepository, times(1)).findByEmail("customer@example.com");
        verify(applicationRepository, times(1)).findById(1L);
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void testUploadDocument_WithClaim_Success() throws IOException {
        // Arrange
        Document claimDoc = new Document();
        claimDoc.setId(2L);
        claimDoc.setFileName("claim_evidence.pdf");
        claimDoc.setClaim(claim);

        when(multipartFile.getOriginalFilename()).thenReturn("claim_evidence.pdf");
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(new byte[0]));
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(documentRepository.save(any(Document.class))).thenReturn(claimDoc);

        // Act
        Document result = documentService.uploadDocument(
                multipartFile, "CLAIM_EVIDENCE", null, 1L, "customer@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("claim_evidence.pdf", result.getFileName());
        verify(claimRepository, times(1)).findById(1L);
    }

    @Test
    void testUploadDocument_UserNotFound() throws IOException {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                documentService.uploadDocument(multipartFile, "POLICY_DOCUMENT", 1L, null, "unknown@example.com"));
    }

    @Test
    void testUploadDocument_ApplicationNotFound() throws IOException {
        // Arrange
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                documentService.uploadDocument(multipartFile, "POLICY_DOCUMENT", 999L, null, "customer@example.com"));
    }

    @Test
    void testUploadDocument_ClaimNotFound() throws IOException {
        // Arrange
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(claimRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                documentService.uploadDocument(multipartFile, "CLAIM_EVIDENCE", null, 999L, "customer@example.com"));
    }

    @Test
    void testGetByApplication_Success() {
        // Arrange
        List<Document> docs = Arrays.asList(document);
        when(documentRepository.findByApplicationId(1L)).thenReturn(docs);

        // Act
        List<Document> results = documentService.getByApplication(1L);

        // Assert
        assertEquals(1, results.size());
        assertEquals("policy_doc.pdf", results.get(0).getFileName());
        verify(documentRepository, times(1)).findByApplicationId(1L);
    }

    @Test
    void testGetByApplication_Empty() {
        // Arrange
        when(documentRepository.findByApplicationId(999L)).thenReturn(Arrays.asList());

        // Act
        List<Document> results = documentService.getByApplication(999L);

        // Assert
        assertEquals(0, results.size());
    }

    @Test
    void testGetByClaim_Success() {
        // Arrange
        Document claimDoc = new Document();
        claimDoc.setId(2L);
        claimDoc.setFileName("evidence.pdf");
        claimDoc.setClaim(claim);

        List<Document> docs = Arrays.asList(claimDoc);
        when(documentRepository.findByClaimId(1L)).thenReturn(docs);

        // Act
        List<Document> results = documentService.getByClaim(1L);

        // Assert
        assertEquals(1, results.size());
        assertEquals("evidence.pdf", results.get(0).getFileName());
    }

    @Test
    void testGetById_Success() {
        // Arrange
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        // Act
        Document result = documentService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("policy_doc.pdf", result.getFileName());
        verify(documentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetById_NotFound() {
        // Arrange
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> documentService.getById(999L));
    }
}

