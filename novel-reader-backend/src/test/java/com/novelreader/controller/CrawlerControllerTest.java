package com.novelreader.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novelreader.crawler.BaseCrawler;
import com.novelreader.config.JwtUtil;
import com.novelreader.entity.CrawlerConfig;
import com.novelreader.repository.FavoriteRepository;
import com.novelreader.repository.NovelRepository;
import com.novelreader.service.CrawlerConfigService;
import com.novelreader.service.CrawlerScheduler;
import com.novelreader.service.CrawlerTaskManager;
import com.novelreader.service.NovelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CrawlerController.class)
@ActiveProfiles("test")
public class CrawlerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NovelService novelService;

    @MockBean
    private NovelRepository novelRepository;

    @MockBean
    private CrawlerConfigService crawlerConfigService;

    @MockBean
    private CrawlerScheduler crawlerScheduler;

    @MockBean
    private CrawlerTaskManager crawlerTaskManager;

    @MockBean
    private List<BaseCrawler> crawlers;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FavoriteRepository favoriteRepository;

    @BeforeEach
    void setUp() {
        when(crawlers.iterator()).thenReturn(Collections.emptyIterator());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetNovelsPage_AllParameters() throws Exception {
        Page<com.novelreader.entity.Novel> mockPage = new PageImpl<>(Collections.emptyList());
        when(novelRepository.searchNovels(any(), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/crawler/novels/page")
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
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetNovelsPage_DefaultParameters() throws Exception {
        Page<com.novelreader.entity.Novel> mockPage = new PageImpl<>(Collections.emptyList());
        when(novelRepository.searchNovels(any(), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/crawler/novels/page"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetCrawlerStatus() throws Exception {
        CrawlerConfig config = new CrawlerConfig();
        config.setPlatform("ciweimao");
        config.setEnabled(1);
        when(crawlerConfigService.findByPlatform("ciweimao")).thenReturn(config);
        when(crawlerTaskManager.isRunning("ciweimao")).thenReturn(false);

        mockMvc.perform(get("/api/crawler/status/ciweimao"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetAllCrawlers() throws Exception {
        mockMvc.perform(get("/api/crawler/crawlers"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testHealth() throws Exception {
        mockMvc.perform(get("/api/crawler/health"))
                .andExpect(status().isOk());
    }
}
