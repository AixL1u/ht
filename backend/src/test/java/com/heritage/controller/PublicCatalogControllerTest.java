package com.heritage.controller;

import com.heritage.repo.HeritageCategoryRepository;
import com.heritage.repo.HeritageProjectRepository;
import com.heritage.repo.InheritorRepository;
import com.heritage.repo.MediaAssetRepository;
import com.heritage.repo.ProjectInheritorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicCatalogController.class)
public class PublicCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HeritageCategoryRepository categoryRepository;

    @MockBean
    private HeritageProjectRepository projectRepository;

    @MockBean
    private InheritorRepository inheritorRepository;

    @MockBean
    private ProjectInheritorRepository projectInheritorRepository;

    @MockBean
    private MediaAssetRepository mediaRepository;

    @Test
    void testGetCategories() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetProjects() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk());
    }
}
