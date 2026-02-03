package com.heritage.repo;

import com.heritage.search.ProjectDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectSearchRepository extends ElasticsearchRepository<ProjectDocument, Long> {
    List<ProjectDocument> findByNameOrIntroOrHistory(String name, String intro, String history);
}
