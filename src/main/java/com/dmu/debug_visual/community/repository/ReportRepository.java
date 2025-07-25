package com.dmu.debug_visual.community.repository;

import com.dmu.debug_visual.community.entity.Report;
import com.dmu.debug_visual.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByTargetTypeAndTargetIdAndReporter(String targetType, Long targetId, User reporter);
}
