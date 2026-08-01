package com.example.raggemini.controller;

import com.example.raggemini.service.DocumentService;
import com.example.raggemini.service.RagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RagController.class)
class RagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RagService ragService;

    @MockBean
    private DocumentService documentService;

    @Test
    void askReturnsBadRequestForEmptyQuestion() throws Exception {
        mockMvc.perform(post("/api/rag/ask")
                        .contentType("application/json")
                        .content("{\"question\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Question cannot be empty"));
    }

    @Test
    void askReturnsAnswerForValidQuestion() throws Exception {
        when(ragService.ask("What is this?")).thenReturn("It's a RAG demo.");

        mockMvc.perform(post("/api/rag/ask")
                        .contentType("application/json")
                        .content("{\"question\": \"What is this?\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("It's a RAG demo."));
    }

    @Test
    void providerEndpointReportsConfiguredProvider() throws Exception {
        when(ragService.getProvider()).thenReturn("gemini");
        when(ragService.isApiKeyLoaded()).thenReturn(true);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/rag/provider"))
                .andExpect(status().isOk());
    }
}
