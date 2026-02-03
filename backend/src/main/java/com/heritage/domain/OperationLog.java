package com.heritage.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "operation_logs")
public class OperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    private String operation; // e.g., "Delete User"

    @Column
    private String method; // e.g., "com.heritage.controller.AdminController.deleteUser"

    @Column
    private String params; // JSON or stringified params

    @Column
    private Long executionTime; // ms

    @Column
    private String ip;

    @Column
    private Instant createdAt;
}
