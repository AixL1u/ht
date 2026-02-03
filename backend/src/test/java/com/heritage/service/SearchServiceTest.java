package com.heritage.service;

import com.heritage.domain.HeritageProject;
import com.heritage.repo.HeritageCategoryRepository;
import com.heritage.repo.HeritageProjectRepository;
import com.heritage.repo.NewsRepository;
import com.heritage.repo.ProductRepository;
import com.heritage.repo.ProjectSearchRepository;
import com.heritage.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {

    @Mock
    private HeritageProjectRepository projectRepository;

    @Mock
    private HeritageCategoryRepository categoryRepository;

    @Mock // Optional, might be null in service
    private ProjectSearchRepository projectSearchRepository;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SearchService searchService;

    @Test
    void testSearchEmptyKeyword() {
        Map<String, Object> result = searchService.search("");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testSearchWithKeyword() {
        // Mock DB behavior
        when(projectRepository.findAll()).thenReturn(Collections.emptyList());
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(newsRepository.findAll()).thenReturn(Collections.emptyList());
        when(productRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // If ES repo is present, mock it too or handle null
        // Since it's Autowired(required=false), Mockito might inject it or not.
        // But we defined it as @Mock, so it is injected.
        
        Map<String, Object> result = searchService.search("test");
        assertNotNull(result);
        assertEquals(0, (int)result.get("total"));
    }
}
