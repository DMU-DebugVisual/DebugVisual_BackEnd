package com.dmu.debug_visual.collab.domain.repository;

import com.dmu.debug_visual.collab.domain.entity.CodeSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeSessionRepository extends JpaRepository<CodeSession, Long> {
}