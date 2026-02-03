package com.heritage.service;

import com.heritage.domain.Activity;
import com.heritage.domain.Reservation;
import com.heritage.repo.ActivityRepository;
import com.heritage.repo.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ActivityRepository activityRepository;

    public ReservationService(ReservationRepository reservationRepository, ActivityRepository activityRepository) {
        this.reservationRepository = reservationRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional
    public Reservation reserve(Long userId, Long activityId) {
        java.util.List<String> activeStatuses = Arrays.asList("pending", "approved");
        if (reservationRepository.existsByUserIdAndActivityIdAndStatusIn(userId, activityId, activeStatuses)) {
            throw new IllegalStateException("已预约该活动");
        }

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("活动不存在"));

        long used = reservationRepository.countByActivityIdAndStatusIn(activityId, activeStatuses);
        if (used >= activity.getCapacity()) {
            throw new IllegalStateException("名额已满");
        }

        Reservation r = new Reservation();
        r.setUserId(userId);
        r.setActivityId(activityId);
        r.setStatus("pending");
        r.setReservedAt(Instant.now());
        return reservationRepository.save(r);
    }

    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("预约不存在"));
        if (!r.getUserId().equals(userId)) {
            throw new IllegalStateException("无权操作");
        }
        r.setStatus("cancelled");
        reservationRepository.save(r);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<java.util.Map<String, Object>> getUserReservations(Long userId) {
        java.util.List<Reservation> list = reservationRepository.findByUserId(userId);
        // 预先加载所有活动，避免 N+1 查询
        java.util.List<Activity> activities = activityRepository.findAll();
        java.util.Map<Long, Activity> activityMap = activities.stream()
            .collect(java.util.stream.Collectors.toMap(Activity::getId, a -> a));
        
        return list.stream().map(r -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", r.getId());
            map.put("activityId", r.getActivityId());
            map.put("status", r.getStatus());
            map.put("reservedAt", r.getReservedAt() != null ? r.getReservedAt().toString() : null);
            
            if (r.getActivityId() != null) {
                Activity a = activityMap.get(r.getActivityId());
                if (a != null) {
                    map.put("activityTitle", a.getTitle());
                    map.put("location", a.getLocation());
                    map.put("startTime", a.getStartTime() != null ? a.getStartTime().toString() : null);
                    map.put("endTime", a.getEndTime() != null ? a.getEndTime().toString() : null);
                    map.put("cover", a.getCover());
                }
            }
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }
}
