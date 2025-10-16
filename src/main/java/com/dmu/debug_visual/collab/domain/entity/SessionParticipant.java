package com.dmu.debug_visual.collab.domain.entity;

import com.dmu.debug_visual.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_session_id", nullable = false)
    private CodeSession codeSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Permission permission;

    public enum Permission {
        READ_ONLY,
        READ_WRITE
    }

    public void updatePermission(Permission permission) {
     this.permission = permission;
    }

    @Builder
    public SessionParticipant(CodeSession codeSession, User user, Permission permission) {
        this.codeSession = codeSession;
        this.user = user;
        this.permission = permission;
    }
}