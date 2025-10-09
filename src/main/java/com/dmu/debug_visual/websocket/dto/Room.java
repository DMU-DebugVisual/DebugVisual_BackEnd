package com.dmu.debug_visual.websocket.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Room {

    private String roomId;    // 방 고유 ID
    private String ownerId;   // 방 생성자(방장)의 ID
    private Map<String, Permission> participants; // 참여자 ID와 권한 목록

    // 참여자의 권한을 정의하는 enum (열거형)
    public enum Permission {
        READ_ONLY,  // 읽기 전용
        READ_WRITE  // 읽기/쓰기
    }

    @Builder
    public Room(String roomId, String ownerId) {
        this.roomId = roomId;
        this.ownerId = ownerId;
        // 여러 사용자가 동시에 접근해도 안전한 ConcurrentHashMap 사용
        this.participants = new ConcurrentHashMap<>();
        // 방 생성자는 기본적으로 읽기/쓰기 권한을 가집니다.
        this.participants.put(ownerId, Permission.READ_WRITE);
    }

    // 새로운 참여자를 방에 추가하는 메서드 (기본 권한은 읽기 전용)
    public void addParticipant(String userId) {
        this.participants.putIfAbsent(userId, Permission.READ_ONLY);
    }

    // 특정 참여자에게 쓰기 권한을 부여하는 메서드
    public void grantWritePermission(String userId) {
        if (this.participants.containsKey(userId)) {
            this.participants.put(userId, Permission.READ_WRITE);
        }
    }
}