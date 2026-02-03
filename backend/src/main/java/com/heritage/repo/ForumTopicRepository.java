package com.heritage.repo;

import com.heritage.domain.ForumTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumTopicRepository extends JpaRepository<ForumTopic, Long> {
}

