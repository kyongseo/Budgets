package ks.com.budgetmanagementproject.feature.expenditure.service;

import jakarta.mail.internet.MimeMessage;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureGuideResponse;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureRecommendResponse;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Component
@EnableScheduling
@Slf4j
public class ExpenditureSchedulerService {

    private final ExpenditureService expenditureService;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    private static final Set<String> BLOCKED_DOMAINS = Set.of("example.com");

    private boolean deliverable(String email) {
        if (email == null) return false;
        String e = email.trim();
        if (e.isEmpty() || !e.contains("@")) return false;
        String domain = e.substring(e.lastIndexOf('@') + 1).toLowerCase();
        return !BLOCKED_DOMAINS.contains(domain);
    }

    @Transactional
    @Scheduled(cron = "0 40 11 * * ?", zone = "Asia/Seoul")
    public void expenditureRecommendScheduler() {
        List<User> users = userRepository.findAll();
        log.info("====================================");
        log.info("[expenditureRecommendScheduler] 스케줄러 시작");
        log.info("[expenditureRecommendScheduler] 대상 사용자 수: {}", users.size());
        log.info("====================================");

        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        for (User user : users) {
            String to = user.getUsername();

            if (!deliverable(to)) {
                log.warn("[expenditureRecommendScheduler] ❌ 유효하지 않은 이메일 주소로 스킵: {}", to);
                skipCount++;
                continue;
            }

            try {
                log.info("[expenditureRecommendScheduler] 🔄 처리 시작 - 사용자: {}", to);

                ExpenditureRecommendResponse response = expenditureService.expenditureRecommend(user);
                log.info("[expenditureRecommendScheduler]   ├─ 오늘 지출 가능 총액: {}원",
                        String.format("%,d", response.getTodayExpenditurePossibleTotal()));
                log.info("[expenditureRecommendScheduler]   ├─ 추천 카테고리 수: {}",
                        response.getRecommendList() != null ? response.getRecommendList().size() : 0);

                // Thymeleaf 템플릿 처리
                Context context = new Context();
                context.setVariable("totalAmount", response.getTodayExpenditurePossibleTotal());
                context.setVariable("recommendList", response.getRecommendList());
                context.setVariable("message", response.getMessage());

                String htmlContent = templateEngine.process("email/expenditure-recommend", context);

                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom("pokj930@naver.com");
                helper.setTo(to);
                helper.setSubject("💰 [Budget Management] 오늘의 지출 추천 안내");
                helper.setText(htmlContent, true);

                mailSender.send(mimeMessage);
                successCount++;
                log.info("[expenditureRecommendScheduler]   └─ ✅ 이메일 발송 성공: {}", to);

            } catch (Exception e) {
                failCount++;
                log.error("[expenditureRecommendScheduler]   └─ ❌ 이메일 발송 실패: {}", to, e);
                log.error("[expenditureRecommendScheduler]      에러 메시지: {}", e.getMessage());
            }
        }

        log.info("====================================");
        log.info("[expenditureRecommendScheduler] 스케줄러 완료");
        log.info("[expenditureRecommendScheduler] ✅ 성공: {}건, ❌ 실패: {}건, ⏭️ 스킵: {}건",
                successCount, failCount, skipCount);
        log.info("====================================");
    }

    @Transactional
    @Scheduled(cron = "0 0 20 * * ?", zone = "Asia/Seoul")
    public void expenditureGuideScheduler() {
        List<User> users = userRepository.findAll();
        log.info("====================================");
        log.info("[expenditureGuideScheduler] 스케줄러 시작");
        log.info("[expenditureGuideScheduler] 대상 사용자 수: {}", users.size());
        log.info("====================================");

        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        for (User user : users) {
            String to = user.getUsername();

            if (!deliverable(to)) {
                log.warn("[expenditureGuideScheduler] ❌ 유효하지 않은 이메일 주소로 스킵: {}", to);
                skipCount++;
                continue;
            }

            try {
                log.info("[expenditureGuideScheduler] 🔄 처리 시작 - 사용자: {}", to);

                ExpenditureGuideResponse response = expenditureService.expenditureGuide(user);
                log.info("[expenditureGuideScheduler]   ├─ 오늘 총 지출: {}원",
                        String.format("%,d", response.getTotalAmount()));
                log.info("[expenditureGuideScheduler]   ├─ 지출 카테고리 수: {}",
                        response.getGuideList() != null ? response.getGuideList().size() : 0);

                // Thymeleaf 템플릿 처리
                Context context = new Context();
                context.setVariable("totalAmount", response.getTotalAmount());
                context.setVariable("guideList", response.getGuideList());

                String htmlContent = templateEngine.process("email/expenditure-guide", context);

                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom("pokj930@naver.com");
                helper.setTo(to);
                helper.setSubject("📝 [Budget Management] 오늘의 지출 내역 안내");
                helper.setText(htmlContent, true);

                mailSender.send(mimeMessage);
                successCount++;
                log.info("[expenditureGuideScheduler]   └─ ✅ 이메일 발송 성공: {}", to);

            } catch (Exception e) {
                failCount++;
                log.error("[expenditureGuideScheduler]   └─ ❌ 이메일 발송 실패: {}", to, e);
                log.error("[expenditureGuideScheduler]      에러 메시지: {}", e.getMessage());
            }
        }

        log.info("====================================");
        log.info("[expenditureGuideScheduler] 스케줄러 완료");
        log.info("[expenditureGuideScheduler] ✅ 성공: {}건, ❌ 실패: {}건, ⏭️ 스킵: {}건",
                successCount, failCount, skipCount);
        log.info("====================================");
    }
}