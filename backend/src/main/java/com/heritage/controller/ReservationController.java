package com.heritage.controller;

import com.heritage.domain.Reservation;
import com.heritage.repo.ReservationRepository;
import com.heritage.service.ReservationService;
import com.heritage.repo.UserRepository;
import com.heritage.domain.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import com.heritage.config.RateLimiterService;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final RateLimiterService rateLimiterService;
    private final UserRepository userRepository;

    @Value("${app.ratelimit.reservation.limit:10}")
    private int reservationLimit;
    @Value("${app.ratelimit.reservation.windowMs:60000}")
    private long reservationWindowMs;

    public ReservationController(ReservationService reservationService, ReservationRepository reservationRepository, 
                                 RateLimiterService rateLimiterService, UserRepository userRepository) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
        this.rateLimiterService = rateLimiterService;
        this.userRepository = userRepository;
    }

    public static class ReserveRequest {
        @NotNull
        public Long userId;
    }

    @PostMapping("/activities/{id}/reservations")
    public Reservation reserve(@PathVariable("id") Long activityId, @RequestBody ReserveRequest req, HttpServletRequest httpReq) {
        String ip = httpReq.getRemoteAddr();
        String key = "reservation:" + req.userId + ":" + ip;
        rateLimiterService.check(key, reservationLimit, reservationWindowMs);
        return reservationService.reserve(req.userId, activityId);
    }

    @GetMapping("/reservations")
    public List<java.util.Map<String, Object>> myReservations(@RequestParam("userId") Long userId) {
        return reservationService.getUserReservations(userId);
    }

    @DeleteMapping("/reservations/{id}")
    public void cancel(@PathVariable("id") Long id, @RequestParam(value = "userId", required = false) Long userId, Authentication auth) {
        Long currentUserId = userId;
        if (currentUserId == null && auth != null) {
            String username = auth.getName();
            currentUserId = userRepository.findByUsername(username)
                    .map(User::getId)
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        }
        if (currentUserId == null) {
            throw new IllegalArgumentException("缺少 userId");
        }
        reservationService.cancelReservation(currentUserId, id);
    }
}

