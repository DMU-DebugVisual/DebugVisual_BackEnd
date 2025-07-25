package com.dmu.debug_visual.community.service;

import com.dmu.debug_visual.community.dto.ReportRequestDTO;
import com.dmu.debug_visual.community.dto.ReportResponseDTO;
import com.dmu.debug_visual.community.entity.Report;
import com.dmu.debug_visual.community.repository.CommentRepository;
import com.dmu.debug_visual.community.repository.PostRepository;
import com.dmu.debug_visual.community.repository.ReportRepository;
import com.dmu.debug_visual.user.User;
import com.dmu.debug_visual.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public void report(ReportRequestDTO dto, User user) {
        if (!dto.getTargetType().equals("POST") && !dto.getTargetType().equals("COMMENT")) {
            throw new IllegalArgumentException("지원하지 않는 신고 대상");
        }

        boolean alreadyReported = reportRepository
                .findByTargetTypeAndTargetIdAndReporter(dto.getTargetType(), dto.getTargetId(), user)
                .isPresent();

        if (alreadyReported) {
            throw new RuntimeException("이미 신고한 대상입니다.");
        }

        // 관리자에게 알림 전송
        User admin = userRepository.findByRole(User.Role.ADMIN).get(0); // 간단 예시
        notificationService.notify(admin, user.getName() + "님이 " + dto.getTargetType() + "를 신고했습니다.");


        Report report = Report.builder()
                .targetType(dto.getTargetType())
                .targetId(dto.getTargetId())
                .reason(dto.getReason())
                .reporter(user)
                .build();

        reportRepository.save(report);
    }

    public List<ReportResponseDTO> getAllReports() {
        return reportRepository.findAll().stream()
                .map(report -> {
                    String preview = getTargetPreview(report);
                    return ReportResponseDTO.builder()
                            .id(report.getId())
                            .targetType(report.getTargetType())
                            .targetId(report.getTargetId())
                            .reporterName(report.getReporter().getName())
                            .reason(report.getReason())
                            .reportedAt(report.getReportedAt())
                            .targetPreview(preview)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String getTargetPreview(Report report) {
        if (report.getTargetType().equals("POST")) {
            return postRepository.findById(report.getTargetId())
                    .map(post -> post.getTitle() + " - " + post.getContent().substring(0, Math.min(30, post.getContent().length())))
                    .orElse("(삭제된 게시글)");
        } else if (report.getTargetType().equals("COMMENT")) {
            return commentRepository.findById(report.getTargetId())
                    .map(comment -> comment.getContent().substring(0, Math.min(30, comment.getContent().length())))
                    .orElse("(삭제된 댓글)");
        }
        return "(알 수 없음)";
    }
}
