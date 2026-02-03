package com.heritage.repo;

import com.heritage.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByUserIdAndActivityIdAndStatusIn(Long userId, Long activityId, List<String> statuses);

    @Query("select count(r) from Reservation r where r.activityId = ?1 and r.status in ?2")
    long countByActivityIdAndStatusIn(Long activityId, List<String> statuses);

    List<Reservation> findByUserId(Long userId);
}

