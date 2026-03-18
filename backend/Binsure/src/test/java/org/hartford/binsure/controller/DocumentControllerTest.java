package org.hartford.binsure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hartford.binsure.entity.Document;
import org.hartford.binsure.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private Authentication authentication;

    private Document document;

    @BeforeEach
    void setUp() {
        document = new Document();
        document.setId(1L);
        document.setFileName("policy.pdf");
        document.setFileType("application/pdf");
    }

    @Test
    void testUpload_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "policy.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(documentService.uploadDocument(any(), anyString(), any(), any(), anyString()))
                .thenReturn(document);

        mockMvc.perform(multipart("/api/v1/documents/upload")
                .file(file)
                .param("documentType", "POLICY_DOCUMENT")
                .param("applicationId", "1"))
                .andExpect(status().isCreated());

        verify(documentService, times(1)).uploadDocument(any(), anyString(), any(), any(), anyString());
    }

    @Test
    void testGetByApplication_Success() throws Exception {
        when(documentService.getByApplication(1L))
                .thenReturn(Arrays.asList(document));

        mockMvc.perform(get("/api/v1/documents/application/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(documentService, times(1)).getByApplication(1L);
    }

    @Test
    void testGetByClaim_Success() throws Exception {
        when(documentService.getByClaim(1L))
                .thenReturn(Arrays.asList(document));

        mockMvc.perform(get("/api/v1/documents/claim/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(documentService, times(1)).getByClaim(1L);
    }
}

