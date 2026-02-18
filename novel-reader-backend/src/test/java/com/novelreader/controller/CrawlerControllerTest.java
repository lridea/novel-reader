package com.novelreader.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CrawlerController 单元测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CrawlerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetNovelsPage_AllParameters() throws Exception {
        String response = mockMvc.perform(get("/api/crawler/novels/page")
                        .param("page", "0")
                        .param("size", "10")
                        .param("platform", "ciweimao")
                        .param("keyword", "修仙")
                        .param("status", "1")
                        .param("tag", "玄幻")
                        .param("wordCountMin", "10w")
                        .param("wordCountMax", "200w")
                        .param("sortBy", "wordCount")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    public void testGetNovelsPage_OnlyStatus() throws Exception {
        String response = mockMvc.perform(get("/api/crawler/novels/page")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    public void testGetNovelsPage_OnlyTag() throws Exception {
        String response = mockMvc.perform(get("/api/crawler/novels/page")
                        .param("page", "0")
                        .param("size", "10")
                        .param("tag", "玄幻"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    public void testGetNovelsPage_SortByWordCount() throws Exception {
        String response = mockMvc.perform(get("/api/crawler/novels/page")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "wordCount")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    public void testGetNovelsPage_SortByUpdateTime() throws Exception {
        String response = mockMvc.perform(get("/api/crawler/novels/page")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "updateTime")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    public void testGetNovelsPage_WordCountMinOnly() throws Exception {
        String response = mockMvc.perform(get("/api/crawler/novels/page")
                        .param("page", "0")
                        .param("size", "10")
                        .param("wordCountMin", "10w"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    public void testGetNovelsPage_WordCountMaxOnly() throws Exception {
        String response = mockMvc.perform(get("/api/crawler/novels/page")
                        .param("page", "0")
                        .param("size", "10")
                        .param("wordCountMax", "200w"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    public void testGetNovelsPage_WordCountRange() throws Exception {
        String response = mockMvc.perform(get("/api/crawler/novels/page")
                        .param("page", "0")
                        .param("size", "10")
                        .param("wordCountMin", "10w")
                        .param("wordCountMax", "200w"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    public void testGetNovelsPage_WordCountInvalid() throws Exception {
        String response = mockMvc.perform(get("/api/crawler/novels/page")
                        .param("page", "0")
                        .param("size", "10")
                        .param("wordCountMin", "invalid"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
    }

    @Test
    public void testGetNovelsPage_DefaultParameters() throws Exception {
        String response = mockMvc.perform(get("/api/crawler/novels/page"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    public void testGetCrawlerStatus() throws Exception {
        String response = mockMvc.perform(get("/api/crawler/status/ciweimao"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
    }

    @Test
    public void testGetAllConfigs() throws Exception {
        String response = mockMvc.perform(get("/api/crawler/configs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
    }

    @Test
    public void testGetAllCrawlers() throws Exception {
        String response = mockMvc.perform(get("/api/crawler/crawlers"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
    }

    @Test
    public void testHealth() throws Exception {
        String response = mockMvc.perform(get("/api/crawler/health"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
    }
}
