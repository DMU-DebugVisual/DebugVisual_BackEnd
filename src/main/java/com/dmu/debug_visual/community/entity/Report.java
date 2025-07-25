package com.dmu.debug_visual.community.entity;

import com.dmu.debug_visual.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "report", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"targetType", "targetId", "reporter_id"})
})
public class Report {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 게시글 or 댓글
    @Column(nullable = false)
    private String targetType;

    @Column(nullable = false)
    private Long targetId;

    @ManyToOne(optional = false)
    private User reporter;

    @Column(nullable = false)
    private String reason;

    private LocalDateTime reportedAt;

    @PrePersist
    public void prePersist() {
        this.reportedAt = LocalDateTime.now();
    }
}
