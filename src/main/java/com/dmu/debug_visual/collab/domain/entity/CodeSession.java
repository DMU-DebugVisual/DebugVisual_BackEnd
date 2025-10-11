package com.dmu.debug_visual.collab.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sessionId; // 웹소켓 통신 등에 사용할 고유 ID

    @Column(nullable = false)
    private String sessionName; // 사용자에게 보여질 세션 이름 (예: "main.java", "알고리즘 문제풀이")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room; // 이 세션이 속한 방

    @Builder
    public CodeSession(String sessionName, Room room) {
        this.sessionId = UUID.randomUUID().toString();
        this.sessionName = sessionName;
        this.room = room;
    }
}