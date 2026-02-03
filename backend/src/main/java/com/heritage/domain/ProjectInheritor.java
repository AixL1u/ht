package com.heritage.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "project_inheritors", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "inheritor_id"})
})
public class ProjectInheritor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "inheritor_id", nullable = false)
    private Long inheritorId;
}

