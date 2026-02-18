package com.novelreader.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novelreader.config.JwtUtil;
import com.novelreader.repository.NovelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TagsController.class)
@ActiveProfiles("test")
public class TagsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NovelRepository novelRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetAllTags() throws Exception {
        when(novelRepository.getAllTags()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/crawler/novels/tags"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetTagsByPlatform() throws Exception {
        when(novelRepository.getTagsByPlatform("ciweimao")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/crawler/novels/tags/platform/ciweimao"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetTagsByPlatform_NotExists() throws Exception {
        when(novelRepository.getTagsByPlatform("notexists")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/crawler/novels/tags/platform/notexists"))
                .andExpect(status().isOk());
    }
}
