package com.dmu.debug_visual.collab.domain.entity;

import com.dmu.debug_visual.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "collab_room") // DB 테이블 이름을 명확히 지정
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomId; // 기존 DTO의 roomId와 동일한 역할 (외부용 ID)

    @Column(nullable = false)
    private String name; // 방 이름 (예: "알고리즘 스터디")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner; // 방을 생성한 방장

    // 1:N 관계 - 이 방에 속한 코드 세션 목록
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CodeSession> codeSessions = new ArrayList<>();

    // 1:N 관계 - 이 방에 참여한 참여자 목록
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomParticipant> participants = new ArrayList<>();

    @Builder
    public Room(String name, User owner) {
        this.roomId = UUID.randomUUID().toString();
        this.name = name;
        this.owner = owner;
    }
}
