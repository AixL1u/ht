package com.heritage.service;

import com.heritage.domain.*;
import com.heritage.repo.*;
import com.heritage.search.ProjectDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class SearchService {
    
    @Autowired
    private HeritageProjectRepository heritageProjectRepository;
    
    @Autowired
    private HeritageCategoryRepository heritageCategoryRepository;

    @Autowired
    private ObjectProvider<ProjectSearchRepository> projectSearchRepositoryProvider;
    
    @Autowired
    private NewsRepository newsRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 同步项目数据到 Elasticsearch
     */
    public void syncProjects() {
        ProjectSearchRepository repo = projectSearchRepositoryProvider.getIfAvailable();
        if (repo == null) return;
        
        List<HeritageProject> projects = heritageProjectRepository.findAll();
        List<HeritageCategory> categories = heritageCategoryRepository.findAll();
        Map<Long, String> categoryMap = categories.stream()
            .collect(Collectors.toMap(HeritageCategory::getId, HeritageCategory::getName));
            
        List<ProjectDocument> docs = projects.stream().map(p -> {
            ProjectDocument doc = new ProjectDocument();
            doc.setId(p.getId());
            doc.setName(p.getName());
            doc.setIntro(p.getIntro());
            doc.setHistory(p.getHistory());
            doc.setCategory(categoryMap.getOrDefault(p.getCategoryId(), "未分类"));
            doc.setRegion(p.getRegion());
            return doc;
        }).collect(Collectors.toList());
        
        repo.saveAll(docs);
    }

    /**
     * 全文搜索
     */
    public Map<String, Object> search(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new HashMap<>();
        }
        
        String lowerKeyword = keyword.toLowerCase();
        
        // 搜索项目 (优先使用 ES)
        List<Map<String, Object>> projects = searchProjects(lowerKeyword);
        
        // 搜索资讯
        List<Map<String, Object>> news = searchNews(lowerKeyword);
        
        // 搜索商品
        List<Map<String, Object>> products = searchProducts(lowerKeyword);
        
        // 搜索用户
        List<Map<String, Object>> users = searchUsers(lowerKeyword);
        
        Map<String, Object> result = new HashMap<>();
        result.put("projects", projects);
        result.put("news", news);
        result.put("products", products);
        result.put("users", users);
        result.put("total", projects.size() + news.size() + products.size() + users.size());
        
        return result;
    }
    
    /**
     * 搜索项目
     */
    private List<Map<String, Object>> searchProjects(String keyword) {
        // Try Elasticsearch first
        /*
        ProjectSearchRepository repo = projectSearchRepositoryProvider.getIfAvailable();
        if (repo != null) {
            try {
                List<ProjectDocument> docs = repo.findByNameOrIntroOrHistory(keyword, keyword, keyword);
                if (!docs.isEmpty()) {
                    return docs.stream().map(doc -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", doc.getId());
                        item.put("type", "project");
                        item.put("title", doc.getName());
                        item.put("description", doc.getIntro());
                        item.put("category", doc.getCategory());
                        item.put("url", "/projects/" + doc.getId());
                        return item;
                    }).limit(10).collect(Collectors.toList());
                }
            } catch (Exception e) {
                // Fallback to DB on error
                System.err.println("ES search failed: " + e.getMessage());
            }
        }
        */

        // Fallback to Database
        List<HeritageProject> projects = heritageProjectRepository.findAll();
        List<HeritageCategory> categories = heritageCategoryRepository.findAll();
        Map<Long, String> categoryMap = categories.stream()
            .collect(Collectors.toMap(HeritageCategory::getId, HeritageCategory::getName));
        
        return projects.stream()
            .filter(p -> {
                String catName = categoryMap.getOrDefault(p.getCategoryId(), "");
                return (p.getName() != null && p.getName().toLowerCase().contains(keyword)) ||
                       (p.getIntro() != null && p.getIntro().toLowerCase().contains(keyword)) ||
                       (catName != null && catName.toLowerCase().contains(keyword));
            })
            .map(p -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", p.getId());
                item.put("type", "project");
                item.put("title", p.getName());
                item.put("description", p.getIntro());
                item.put("category", categoryMap.getOrDefault(p.getCategoryId(), "未分类"));
                item.put("url", "/projects/" + p.getId());
                return item;
            })
            .limit(10)
            .collect(Collectors.toList());
    }
    
    /**
     * 搜索资讯
     */
    private List<Map<String, Object>> searchNews(String keyword) {
        List<News> newsList = newsRepository.findAll();
        
        return newsList.stream()
            .filter(n -> 
                (n.getTitle() != null && n.getTitle().toLowerCase().contains(keyword)) ||
                (n.getContent() != null && n.getContent().toLowerCase().contains(keyword))
            )
            .map(n -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", n.getId());
                item.put("type", "news");
                item.put("title", n.getTitle());
                item.put("description", n.getContent() != null ? 
                    n.getContent().substring(0, Math.min(100, n.getContent().length())) : "");
                item.put("publishTime", n.getPublishTime());
                item.put("url", "/news");
                return item;
            })
            .limit(10)
            .collect(Collectors.toList());
    }
    
    /**
     * 搜索商品
     */
    private List<Map<String, Object>> searchProducts(String keyword) {
        List<Product> products = productRepository.findAll();
        
        return products.stream()
            .filter(p -> 
                (p.getName() != null && p.getName().toLowerCase().contains(keyword)) ||
                (p.getDescription() != null && p.getDescription().toLowerCase().contains(keyword))
            )
            .map(p -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", p.getId());
                item.put("type", "product");
                item.put("title", p.getName());
                item.put("description", p.getDescription());
                item.put("price", p.getPrice());
                item.put("url", "/shop");
                return item;
            })
            .limit(10)
            .collect(Collectors.toList());
    }
    
    /**
     * 搜索用户
     */
    private List<Map<String, Object>> searchUsers(String keyword) {
        List<User> users = userRepository.findAll();
        
        return users.stream()
            .filter(u -> 
                (u.getUsername() != null && u.getUsername().toLowerCase().contains(keyword)) ||
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword))
            )
            .map(u -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", u.getId());
                item.put("type", "user");
                item.put("title", u.getUsername());
                item.put("description", u.getEmail());
                item.put("url", "/me");
                return item;
            })
            .limit(5)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取搜索建议
     */
    public List<String> getSearchSuggestions(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerPrefix = prefix.toLowerCase();
        Set<String> suggestions = new LinkedHashSet<>();
        
        // 从项目中提取建议
        heritageProjectRepository.findAll().stream()
            .map(HeritageProject::getName)
            .filter(name -> name != null && name.toLowerCase().startsWith(lowerPrefix))
            .limit(3)
            .forEach(suggestions::add);
        
        // 从资讯中提取建议
        newsRepository.findAll().stream()
            .map(News::getTitle)
            .filter(title -> title != null && title.toLowerCase().startsWith(lowerPrefix))
            .limit(3)
            .forEach(suggestions::add);
        
        // 从商品中提取建议
        productRepository.findAll().stream()
            .map(Product::getName)
            .filter(name -> name != null && name.toLowerCase().startsWith(lowerPrefix))
            .limit(3)
            .forEach(suggestions::add);
        
        return new ArrayList<>(suggestions);
    }
    
    /**
     * 获取热门搜索词
     */
    public List<String> getHotSearches() {
        // 这里可以从数据库的搜索历史表中获取
        // 现在返回预定义的热门搜索词
        return Arrays.asList(
            "京剧",
            "书法",
            "绘画",
            "手工艺",
            "民间艺术",
            "传统文化",
            "非遗项目"
        );
    }
}
