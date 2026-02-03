package com.heritage.controller;

import com.heritage.domain.*;
import com.heritage.repo.*;
import com.heritage.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin(origins = "http://localhost:5173")
public class GraphController {

    @Autowired
    private HeritageProjectRepository projectRepository;
    
    @Autowired
    private InheritorRepository inheritorRepository;
    
    @Autowired
    private HeritageCategoryRepository categoryRepository;
    
    @Autowired
    private ProjectInheritorRepository projectInheritorRepository;

    @GetMapping
    public ResponseEntity<?> getGraphData() {
        List<HeritageProject> projects = projectRepository.findAll();
        List<Inheritor> inheritors = inheritorRepository.findAll();
        List<HeritageCategory> categories = categoryRepository.findAll();

        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();
        Set<String> addedNodes = new HashSet<>();

        // 1. Add Categories (Root Nodes)
        Map<Long, String> catMap = new HashMap<>();
        for (HeritageCategory c : categories) {
            String id = "C_" + c.getId();
            catMap.put(c.getId(), id);
            
            if (addedNodes.add(id)) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", id);
                node.put("name", c.getName());
                node.put("category", 0); // 0: Category
                node.put("symbolSize", 30);
                node.put("value", projects.stream().filter(p -> p.getCategoryId().equals(c.getId())).count());
                nodes.add(node);
            }
        }

        // 2. Add Projects
        Map<Long, String> projMap = new HashMap<>();
        for (HeritageProject p : projects) {
            String id = "P_" + p.getId();
            projMap.put(p.getId(), id);

            if (addedNodes.add(id)) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", id);
                node.put("name", p.getName());
                node.put("category", 1); // 1: Project
                node.put("symbolSize", 20);
                nodes.add(node);
            }

            // Link: Project -> Category
            if (p.getCategoryId() != null && catMap.containsKey(p.getCategoryId())) {
                Map<String, Object> link = new HashMap<>();
                link.put("source", id);
                link.put("target", catMap.get(p.getCategoryId()));
                link.put("name", "属于");
                links.add(link);
            }
        }

        // 3. Add Inheritors
        for (Inheritor i : inheritors) {
            String id = "I_" + i.getId();
            
            if (addedNodes.add(id)) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", id);
                node.put("name", i.getName());
                node.put("category", 2); // 2: Inheritor
                node.put("symbolSize", 15);
                nodes.add(node);
            }

            // Link: Inheritor -> Project (through ProjectInheritor)
            List<ProjectInheritor> projectInheritors = projectInheritorRepository.findByInheritorId(i.getId());
            for (ProjectInheritor pi : projectInheritors) {
                if (projMap.containsKey(pi.getProjectId())) {
                    Map<String, Object> link = new HashMap<>();
                    link.put("source", id);
                    link.put("target", projMap.get(pi.getProjectId()));
                    link.put("name", "传承");
                    links.add(link);
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("nodes", nodes);
        data.put("links", links);
        data.put("categories", Arrays.asList(
            Map.of("name", "分类"),
            Map.of("name", "非遗项目"),
            Map.of("name", "传承人")
        ));

        return ResponseEntity.ok(new ApiResponse(0, "success", data, Instant.now().toEpochMilli()));
    }
}
