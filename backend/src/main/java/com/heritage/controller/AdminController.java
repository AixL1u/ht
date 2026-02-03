package com.heritage.controller;

import com.heritage.domain.*;
import com.heritage.repo.*;
import com.heritage.service.OrderService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.alibaba.excel.EasyExcel;
import com.heritage.dto.ProjectExportDTO;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.io.IOException;
import java.util.stream.Collectors;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.heritage.annotation.Log;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final HeritageCategoryRepository categoryRepository;
    private final HeritageProjectRepository projectRepository;
    private final InheritorRepository inheritorRepository;
    private final ProjectInheritorRepository projectInheritorRepository;
    private final MediaAssetRepository mediaRepository;
    private final NewsRepository newsRepository;
    private final VideoRepository videoRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final BannerRepository bannerRepository;
    private final SiteSettingRepository siteSettingRepository;
    private final ReservationRepository reservationRepository;
    private final ActivityRepository activityRepository;
    private final ForumTopicRepository topicRepository;
    private final PostRepository postRepository;

    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, HeritageCategoryRepository categoryRepository, HeritageProjectRepository projectRepository, InheritorRepository inheritorRepository, ProjectInheritorRepository projectInheritorRepository, MediaAssetRepository mediaRepository, NewsRepository newsRepository, VideoRepository videoRepository, ProductCategoryRepository productCategoryRepository, ProductRepository productRepository, OrderRepository orderRepository, OrderService orderService, BannerRepository bannerRepository, SiteSettingRepository siteSettingRepository, ReservationRepository reservationRepository, ActivityRepository activityRepository, ForumTopicRepository topicRepository, PostRepository postRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.projectRepository = projectRepository;
        this.inheritorRepository = inheritorRepository;
        this.projectInheritorRepository = projectInheritorRepository;
        this.mediaRepository = mediaRepository;
        this.newsRepository = newsRepository;
        this.videoRepository = videoRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.bannerRepository = bannerRepository;
        this.siteSettingRepository = siteSettingRepository;
        this.reservationRepository = reservationRepository;
        this.activityRepository = activityRepository;
        this.topicRepository = topicRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public List<User> listUsers() { return userRepository.findAll(); }

    @GetMapping("/users/search")
        public Page<User> searchUsers(@RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "size", defaultValue = "10") int size,
                                  @RequestParam(value = "keyword", required = false) String keyword) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        if (keyword != null && !keyword.isBlank()) {
            return userRepository.findByUsernameContainingIgnoreCase(keyword, pageable);
        }
        return userRepository.findAll(pageable);
    }

    public static class CreateUserReq { public String username; public String password; public String role; public String email; public String phone; public String status; }
    @PostMapping("/users/create")
        public User createUser(@RequestBody CreateUserReq req) {
        User u = new User();
        u.setUsername(req.username);
        u.setPasswordHash(passwordEncoder.encode(req.password != null ? req.password : "123456"));
        u.setRole(req.role);
        u.setEmail(req.email);
        u.setPhone(req.phone);
        u.setStatus(req.status);
        u.setCreatedAt(Instant.now());
        return userRepository.save(u);
    }

    @PostMapping("/users")
        public User addUser(@RequestBody User u) { return userRepository.save(u); }
    @PutMapping("/users/{id}")
        public User updateUser(@PathVariable("id") Long id, @RequestBody User u) { u.setId(id); return userRepository.save(u); }

    public static class UpdateUserPasswordReq { public String newPassword; }
    @PutMapping("/users/{id}/password")
        @Log("修改用户密码")
    public User updateUserPassword(@PathVariable("id") Long id, @RequestBody UpdateUserPasswordReq req) {
        User u = userRepository.findById(id).orElseThrow();
        u.setPasswordHash(passwordEncoder.encode(req.newPassword));
        return userRepository.save(u);
    }

    @DeleteMapping("/users/{id}")
    @Log("删除用户")
    public com.heritage.api.ApiResponse<Void> deleteUser(@PathVariable("id") Long id) { userRepository.deleteById(id); return com.heritage.api.ApiResponse.success(null); }

    @PostMapping("/categories")
        public HeritageCategory addCategory(@RequestBody HeritageCategory c) { return categoryRepository.save(c); }
    @GetMapping("/projects")
        public List<HeritageProject> listProjects() { return projectRepository.findAll(); }

    @GetMapping("/projects/search")
        public Page<HeritageProject> searchProjects(@RequestParam(value = "page", defaultValue = "0") int page,
                                                @RequestParam(value = "size", defaultValue = "10") int size,
                                                @RequestParam(value = "keyword", required = false) String keyword,
                                                @RequestParam(value = "categoryId", required = false) Long categoryId) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        if (categoryId != null && keyword != null && !keyword.isBlank()) {
            return projectRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, keyword, pageable);
        } else if (categoryId != null) {
            return projectRepository.findByCategoryId(categoryId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            return projectRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            return projectRepository.findAll(pageable);
        }
    }

    @GetMapping("/projects/export")
        public void exportProjects(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("非遗项目数据", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        List<HeritageProject> list = projectRepository.findAll();
        List<ProjectExportDTO> exportList = list.stream().map(p -> {
            ProjectExportDTO dto = new ProjectExportDTO();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setIntro(p.getIntro());
            dto.setRegion(p.getRegion());
            dto.setCreatedAt(p.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());

        EasyExcel.write(response.getOutputStream(), ProjectExportDTO.class).sheet("项目列表").doWrite(exportList);
    }

    @PostMapping("/projects")
        public HeritageProject addProject(@RequestBody HeritageProject p) { p.setCreatedAt(Instant.now()); return projectRepository.save(p); }
    @PutMapping("/projects/{id}")
        public HeritageProject updateProject(@PathVariable("id") Long id, @RequestBody HeritageProject p) { p.setId(id); p.setUpdatedAt(Instant.now()); return projectRepository.save(p); }
    @DeleteMapping("/projects/{id}")
    @Log("删除项目")
    public com.heritage.api.ApiResponse<Void> deleteProject(@PathVariable("id") Long id) { projectRepository.deleteById(id); return com.heritage.api.ApiResponse.success(null); }

    @PostMapping("/inheritors")
        public Inheritor addInheritor(@RequestBody Inheritor i) { return inheritorRepository.save(i); }
    @GetMapping("/inheritors")
        public List<Inheritor> listInheritors() { return inheritorRepository.findAll(); }

    @GetMapping("/inheritors/search")
        public Page<Inheritor> searchInheritors(@RequestParam(value = "page", defaultValue = "0") int page,
                                            @RequestParam(value = "size", defaultValue = "10") int size,
                                            @RequestParam(value = "keyword", required = false) String keyword) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        if (keyword != null && !keyword.isBlank()) {
            return inheritorRepository.findByNameContainingIgnoreCase(keyword, pageable);
        }
        return inheritorRepository.findAll(pageable);
    }
    @DeleteMapping("/inheritors/{id}")
    public com.heritage.api.ApiResponse<Void> deleteInheritor(@PathVariable("id") Long id) { inheritorRepository.deleteById(id); return com.heritage.api.ApiResponse.success(null); }
    
    @PutMapping("/inheritors/{id}")
    public Inheritor updateInheritor(@PathVariable("id") Long id, @RequestBody Inheritor i) {
        Inheritor existing = inheritorRepository.findById(id).orElseThrow();
        existing.setName(i.getName());
        existing.setBio(i.getBio());
        existing.setSkills(i.getSkills());
        existing.setContact(i.getContact());
        existing.setRegion(i.getRegion());
        existing.setLevel(i.getLevel());
        existing.setAvatarUrl(i.getAvatarUrl());
        return inheritorRepository.save(existing);
    }
    
    @PostMapping("/project-inheritors")
        public ProjectInheritor bindProjectInheritor(@RequestBody ProjectInheritor pi) { return projectInheritorRepository.save(pi); }

    @PostMapping("/media")
        public MediaAsset addMedia(@RequestBody MediaAsset m) { m.setCreatedAt(Instant.now()); return mediaRepository.save(m); }
    @PutMapping("/media/{id}")
        public MediaAsset updateMedia(@PathVariable("id") Long id, @RequestBody MediaAsset m) { m.setId(id); return mediaRepository.save(m); }
    @DeleteMapping("/media/{id}")
    public com.heritage.api.ApiResponse<Void> deleteMedia(@PathVariable("id") Long id) { mediaRepository.deleteById(id); return com.heritage.api.ApiResponse.success(null); }

    @GetMapping("/media/search")
        public Page<MediaAsset> searchMedia(@RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "size", defaultValue = "10") int size,
                                        @RequestParam(value = "keyword", required = false) String keyword,
                                        @RequestParam(value = "type", required = false) String type) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        if (type != null && !type.isBlank() && keyword != null && !keyword.isBlank()) {
            return mediaRepository.findByTypeAndTitleContainingIgnoreCase(type, keyword, pageable);
        } else if (type != null && !type.isBlank()) {
            return mediaRepository.findByType(type, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            return mediaRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else {
            return mediaRepository.findAll(pageable);
        }
    }

    @PostMapping("/news")
        public News addNews(@RequestBody News n) { n.setPublishTime(Instant.now()); return newsRepository.save(n); }
    @PutMapping("/news/{id}")
        public News updateNews(@PathVariable("id") Long id, @RequestBody News n) { n.setId(id); return newsRepository.save(n); }
    @DeleteMapping("/news/{id}")
    public com.heritage.api.ApiResponse<Void> deleteNews(@PathVariable("id") Long id) { newsRepository.deleteById(id); return com.heritage.api.ApiResponse.success(null); }

    @GetMapping("/news/search")
        public Page<News> searchNews(@RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "type", required = false) String type) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        if (type != null && !type.isBlank() && keyword != null && !keyword.isBlank()) {
            return newsRepository.findByTypeAndTitleContainingIgnoreCase(type, keyword, pageable);
        } else if (type != null && !type.isBlank()) {
            return newsRepository.findByType(type, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            return newsRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else {
            return newsRepository.findAll(pageable);
        }
    }

    @PostMapping("/videos")
        public Video addVideo(@RequestBody Video v) { return videoRepository.save(v); }
    @PutMapping("/videos/{id}")
        public Video updateVideo(@PathVariable("id") Long id, @RequestBody Video v) { v.setId(id); return videoRepository.save(v); }
    @DeleteMapping("/videos/{id}")
    public com.heritage.api.ApiResponse<Void> deleteVideo(@PathVariable("id") Long id) { videoRepository.deleteById(id); return com.heritage.api.ApiResponse.success(null); }

    @GetMapping("/videos/search")
        public Page<Video> searchVideos(@RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                    @RequestParam(value = "keyword", required = false) String keyword) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        if (keyword != null && !keyword.isBlank()) {
            return videoRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        }
        return videoRepository.findAll(pageable);
    }

    @PostMapping("/product-categories")
        public ProductCategory addProductCategory(@RequestBody ProductCategory c) { return productCategoryRepository.save(c); }
    @PutMapping("/product-categories/{id}")
        public ProductCategory updateProductCategory(@PathVariable("id") Long id, @RequestBody ProductCategory c) { c.setId(id); return productCategoryRepository.save(c); }

    @PostMapping("/products")
        public Product addProduct(@RequestBody Product p) { return productRepository.save(p); }
    @PutMapping("/products/{id}")
        public Product updateProduct(@PathVariable("id") Long id, @RequestBody Product p) { p.setId(id); return productRepository.save(p); }
    @DeleteMapping("/products/{id}")
    public com.heritage.api.ApiResponse<Void> deleteProduct(@PathVariable("id") Long id) { productRepository.deleteById(id); return com.heritage.api.ApiResponse.success(null); }

    @GetMapping("/products/search")
        public Page<Product> searchProducts(@RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "size", defaultValue = "10") int size,
                                        @RequestParam(value = "keyword", required = false) String keyword,
                                        @RequestParam(value = "categoryId", required = false) Long categoryId) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        if (categoryId != null && keyword != null && !keyword.isBlank()) {
            return productRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, keyword, pageable);
        } else if (categoryId != null) {
            return productRepository.findByCategoryId(categoryId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            return productRepository.findAll(pageable);
        }
    }

    @GetMapping("/orders")
        public List<Order> allOrders() { return orderRepository.findAll(); }
    @GetMapping("/orders/search")
        public Page<Order> searchOrders(@RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                    @RequestParam(value = "userId", required = false) Long userId) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        if (userId != null) {
            return orderRepository.findByUserId(userId, pageable);
        }
        return orderRepository.findAll(pageable);
    }
    public static class UpdateOrderStatusReq { public String status; public String paymentStatus; }
    @PutMapping("/orders/{id}/status")
        public Order updateOrderStatus(@PathVariable("id") Long id, @RequestBody UpdateOrderStatusReq req) { return orderService.updateStatus(id, req.status, req.paymentStatus); }

    @PostMapping("/banners")
        public Banner addBanner(@RequestBody Banner b) { return bannerRepository.save(b); }
    @PutMapping("/banners/{id}")
        public Banner updateBanner(@PathVariable("id") Long id, @RequestBody Banner b) { b.setId(id); return bannerRepository.save(b); }
    @DeleteMapping("/banners/{id}")
    public com.heritage.api.ApiResponse<Void> deleteBanner(@PathVariable("id") Long id) { bannerRepository.deleteById(id); return com.heritage.api.ApiResponse.success(null); }
    @PostMapping("/settings")
        public SiteSetting addSetting(@RequestBody SiteSetting s) { return siteSettingRepository.save(s); }
    @PutMapping("/settings/{id}")
        public SiteSetting updateSetting(@PathVariable("id") Long id, @RequestBody SiteSetting s) { s.setId(id); return siteSettingRepository.save(s); }

    @GetMapping("/reservations")
        public List<Reservation> reservations() { return reservationRepository.findAll(); }
    @GetMapping("/reservations/search")
        public Page<Reservation> searchReservations(@RequestParam(value = "page", defaultValue = "0") int page,
                                                @RequestParam(value = "size", defaultValue = "10") int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return reservationRepository.findAll(pageable);
    }
    public static class UpdateReservationStatusReq { public String status; public Long approvedBy; }
    @PutMapping("/reservations/{id}")
        public Reservation updateReservation(@PathVariable("id") Long id, @RequestBody UpdateReservationStatusReq req) {
        Reservation r = reservationRepository.findById(id).orElseThrow();
        r.setStatus(req.status);
        r.setApprovedBy(req.approvedBy);
        return reservationRepository.save(r);
    }
    @PostMapping("/activities")
        public Activity addActivity(@RequestBody Activity a) { return activityRepository.save(a); }
    
    @PutMapping("/activities/{id}")
    public Activity updateActivity(@PathVariable("id") Long id, @RequestBody Activity a) {
        Activity existing = activityRepository.findById(id).orElseThrow();
        existing.setTitle(a.getTitle());
        existing.setLocation(a.getLocation());
        existing.setStartTime(a.getStartTime());
        existing.setEndTime(a.getEndTime());
        existing.setCapacity(a.getCapacity());
        existing.setStatus(a.getStatus());
        existing.setCover(a.getCover());
        existing.setDescription(a.getDescription());
        return activityRepository.save(existing);
    }
    
    @DeleteMapping("/activities/{id}")
    public com.heritage.api.ApiResponse<Void> deleteActivity(@PathVariable("id") Long id) { 
        activityRepository.deleteById(id); 
        return com.heritage.api.ApiResponse.success(null); 
    }

    @PostMapping("/forum/topics")
        public ForumTopic addTopic(@RequestBody ForumTopic t) { return topicRepository.save(t); }
    @PutMapping("/forum/topics/{id}")
        public ForumTopic updateTopic(@PathVariable("id") Long id, @RequestBody ForumTopic t) { t.setId(id); return topicRepository.save(t); }
    @PutMapping("/forum/posts/{id}/status")
        public Post updatePostStatus(@PathVariable("id") Long id, @RequestBody Post p) { Post origin = postRepository.findById(id).orElseThrow(); origin.setStatus(p.getStatus()); return postRepository.save(origin); }
}

