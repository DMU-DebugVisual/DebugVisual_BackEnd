package com.dmu.debug_visual.community.controller;

import com.dmu.debug_visual.community.dto.ReportRequestDTO;
import com.dmu.debug_visual.community.entity.Report;
import com.dmu.debug_visual.community.repository.ReportRepository;
import com.dmu.debug_visual.community.service.ReportService;
import com.dmu.debug_visual.security.CustomUserDetails;
import com.dmu.debug_visual.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "커뮤니티 - 신고 API", description = "게시글 및 댓글 신고 API")
public class ReportController {

    private final ReportService reportService;
    private final ReportRepository reportRepository;

    @PostMapping
    @Operation(summary = "게시글 또는 댓글 신고")
    public void report(@RequestBody ReportRequestDTO dto,
                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        reportService.report(dto, user);
    }

    @GetMapping("/admin/reports")
    @Operation(summary = "신고 목록 전체 조회 (관리자용)")
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

}
