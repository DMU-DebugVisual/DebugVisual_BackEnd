package com.dmu.debug_visual.collab.domain.repository;

import com.dmu.debug_visual.collab.domain.entity.CodeSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodeSessionRepository extends JpaRepository<CodeSession, Long> {
    Optional<CodeSession> findBySessionId(String sessionId);
}