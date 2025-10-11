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
public class RoomParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

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

    @Builder
    public RoomParticipant(Room room, User user, Permission permission) {
        this.room = room;
        this.user = user;
        this.permission = permission;
    }
}