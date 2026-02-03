package com.heritage.controller;

import com.heritage.domain.*;
import com.heritage.repo.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@RestController
@RequestMapping("/api/forum")
public class PublicForumController {
    private final ForumTopicRepository topicRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository likeRepository;
    private final UserRepository userRepository;

    public PublicForumController(ForumTopicRepository topicRepository, PostRepository postRepository, CommentRepository commentRepository, PostLikeRepository likeRepository, UserRepository userRepository) {
        this.topicRepository = topicRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/topics")
    public List<ForumTopic> topics() { return topicRepository.findAll(); }

    @GetMapping("/posts")
    public List<PostDTO> posts(
            @RequestParam(value = "topicId", required = false) Long topicId,
            @RequestParam(value = "currentUserId", required = false) Long currentUserId) {
        List<Post> posts = topicId == null ? postRepository.findAll() : postRepository.findByTopicId(topicId);
        return posts.stream().map(p -> toDTO(p, currentUserId)).toList();
    }
    
    private PostDTO toDTO(Post p, Long currentUserId) {
        PostDTO dto = new PostDTO();
        dto.id = p.getId();
        dto.topicId = p.getTopicId();
        dto.userId = p.getUserId();
        dto.title = p.getTitle();
        dto.content = p.getContent();
        dto.status = p.getStatus();
        dto.createdAt = p.getCreatedAt();
        dto.likeCount = likeRepository.countByPostId(p.getId());
        dto.commentCount = commentRepository.countByPostId(p.getId());
        dto.liked = currentUserId != null && likeRepository.existsByUserIdAndPostId(currentUserId, p.getId());
        // 处理用户名：匿名则显示"匿名用户"，否则显示真实用户名
        if (Boolean.TRUE.equals(p.getAnonymous())) {
            dto.userName = "匿名用户";
        } else {
            dto.userName = userRepository.findById(p.getUserId())
                    .map(u -> u.getNickname() != null ? u.getNickname() : u.getUsername())
                    .orElse("用户" + p.getUserId());
        }
        return dto;
    }
    
    public static class PostDTO {
        public Long id;
        public Long topicId;
        public Long userId;
        public String userName;
        public String title;
        public String content;
        public String status;
        public java.time.Instant createdAt;
        public long likeCount;
        public long commentCount;
        public boolean liked;
    }

    @GetMapping("/posts/search")
    public Page<Post> searchPosts(@RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "size", defaultValue = "10") int size,
                                  @RequestParam(value = "topicId", required = false) Long topicId,
                                  @RequestParam(value = "keyword", required = false) String keyword) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        if (topicId != null && keyword != null && !keyword.isBlank()) {
            return postRepository.findByTopicIdAndTitleContainingIgnoreCase(topicId, keyword, pageable);
        } else if (topicId != null) {
            return postRepository.findByTopicId(topicId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            return postRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else {
            return postRepository.findAll(pageable);
        }
    }

    public static class PostReq { 
        public Long topicId; 
        public Long userId; 
        public String title; 
        public String content; 
        public Boolean anonymous;
    }

    @PostMapping("/posts")
    public Post createPost(@RequestBody PostReq req) {
        Post p = new Post();
        p.setTopicId(req.topicId);
        p.setUserId(req.userId);
        p.setTitle(req.title);
        p.setContent(req.content);
        p.setStatus("visible");
        p.setCreatedAt(Instant.now());
        p.setAnonymous(req.anonymous != null && req.anonymous);
        return postRepository.save(p);
    }

    public static class CommentReq { public Long postId; public Long userId; public String content; public Long parentId; }

    @PostMapping("/comments")
    public Comment createComment(@RequestBody CommentReq req) {
        Comment c = new Comment();
        c.setPostId(req.postId);
        c.setUserId(req.userId);
        c.setContent(req.content);
        c.setParentId(req.parentId);
        c.setCreatedAt(Instant.now());
        return commentRepository.save(c);
    }

    @DeleteMapping("/posts/{id}")
    @Transactional
    public void deletePost(@PathVariable Long id, @RequestParam("userId") Long userId) {
        Post post = postRepository.findById(id).orElseThrow();
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的留言");
        }
        // 删除相关的点赞和评论
        likeRepository.deleteByPostId(id);
        commentRepository.deleteByPostId(id);
        postRepository.deleteById(id);
    }

    @DeleteMapping("/comments/{id}")
    @Transactional
    public void deleteComment(@PathVariable Long id, @RequestParam("userId") Long userId) {
        Comment comment = commentRepository.findById(id).orElseThrow();
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的评论");
        }
        commentRepository.deleteById(id);
    }

    @PostMapping("/posts/{id}/like")
    public LikeResult like(@PathVariable Long id, @RequestParam("userId") Long userId) {
        // 检查帖子是否存在
        if (!postRepository.existsById(id)) {
            throw new IllegalArgumentException("帖子不存在: " + id);
        }
        
        LikeResult result = new LikeResult();
        if (!likeRepository.existsByUserIdAndPostId(userId, id)) {
            PostLike l = new PostLike();
            l.setUserId(userId);
            l.setPostId(id);
            likeRepository.save(l);
            result.liked = true;
        } else {
            result.liked = false;
            result.message = "您已经点过赞了";
        }
        result.likeCount = likeRepository.countByPostId(id);
        return result;
    }

    @DeleteMapping("/posts/{id}/like")
    @Transactional
    public LikeResult unlike(@PathVariable Long id, @RequestParam("userId") Long userId) {
        likeRepository.deleteByUserIdAndPostId(userId, id);
        LikeResult result = new LikeResult();
        result.liked = false;
        result.likeCount = likeRepository.countByPostId(id);
        return result;
    }
    
    @GetMapping("/posts/{id}/like/check")
    public LikeResult checkLike(@PathVariable Long id, @RequestParam("userId") Long userId) {
        LikeResult result = new LikeResult();
        result.liked = likeRepository.existsByUserIdAndPostId(userId, id);
        result.likeCount = likeRepository.countByPostId(id);
        return result;
    }

    @GetMapping("/posts/{postId}/comments")
    public List<CommentDTO> getComments(@PathVariable Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream().map(c -> {
            CommentDTO dto = new CommentDTO();
            dto.id = c.getId();
            dto.postId = c.getPostId();
            dto.userId = c.getUserId();
            dto.content = c.getContent();
            dto.createdAt = c.getCreatedAt();
            dto.userName = userRepository.findById(c.getUserId())
                    .map(u -> u.getNickname() != null ? u.getNickname() : u.getUsername())
                    .orElse("匿名用户");
            return dto;
        }).toList();
    }

    public static class CommentDTO {
        public Long id;
        public Long postId;
        public Long userId;
        public String userName;
        public String content;
        public java.time.Instant createdAt;
    }
    
    public static class LikeResult {
        public boolean liked;
        public long likeCount;
        public String message = "";
    }
}
