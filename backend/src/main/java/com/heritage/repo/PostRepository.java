package com.heritage.repo;

import com.heritage.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByTopicId(Long topicId);
    List<Post> findByUserId(Long userId);

    Page<Post> findByTopicId(Long topicId, Pageable pageable);
    Page<Post> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Post> findByTopicIdAndTitleContainingIgnoreCase(Long topicId, String keyword, Pageable pageable);
}

