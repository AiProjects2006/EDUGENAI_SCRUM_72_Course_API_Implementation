package com.ailearning.courseservice.entity;

import com.ailearning.courseservice.enums.CourseLevel;
import com.ailearning.courseservice.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    private CourseLevel level;

    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "course",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Module> modules = new ArrayList<>();

    public Course() {
        super();
    }

    public Course(
            Long courseId,
            Subject subject,
            Long creatorId,
            String title,
            String description,
            String thumbnailUrl,
            CourseLevel level,
            CourseStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<Module> modules
    ) {
        super();
        this.courseId = courseId;
        this.subject = subject;
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.level = level;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.modules = modules == null ? new ArrayList<>() : modules;
    }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}