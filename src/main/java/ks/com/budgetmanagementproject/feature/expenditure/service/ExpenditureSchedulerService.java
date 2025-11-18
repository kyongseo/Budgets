package ks.com.budgetmanagementproject.feature.expenditure.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureGuideResponse;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureRecommendResponse;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(
        value = "app.scheduling.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@EnableScheduling
@Slf4j
public class ExpenditureSchedulerService {

    private final ExpenditureService expenditureService;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    private static final Set<String> BLOCKED_DOMAINS = Set.of("example.com");
    private static final String FROM_EMAIL = "pokj930@naver.com";

    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return false;
        }
        String domain = email.substring(email.lastIndexOf('@') + 1).toLowerCase();
        return !BLOCKED_DOMAINS.contains(domain);
    }

    @Transactional
    @Scheduled(cron = "0 40 11 * * ?", zone = "Asia/Seoul")
    public void expenditureRecommendScheduler() {
        List<User> users = userRepository.findAll();
        log.info("[expenditureRecommendScheduler] 시작 - 대상 사용자: {}명", users.size());

        SchedulerResult result = new SchedulerResult();

        for (User user : users) {
            if (!isValidEmail(user.getUsername())) {
                log.warn("유효하지 않은 이메일 스킵: {}", user.getUsername());
                result.skip();
                continue;
            }

            try {
                ExpenditureRecommendResponse response = expenditureService.expenditureRecommend(user);
                sendEmail(user.getUsername(),
                        "💰 [Budget Management] 오늘의 지출 추천 안내",
                        "email/expenditure-recommend",
                        createRecommendContext(response));
                result.success();
                log.info("이메일 발송 성공: {}", user.getUsername());
            } catch (Exception e) {
                result.fail();
                log.error("이메일 발송 실패: {}", user.getUsername(), e);
            }
        }

        log.info("[expenditureRecommendScheduler] 완료 - 성공: {}건, 실패: {}건, 스킵: {}건",
                result.successCount, result.failCount, result.skipCount);
    }

    @Transactional
    @Scheduled(cron = "0 0 20 * * ?", zone = "Asia/Seoul")
    public void expenditureGuideScheduler() {
        List<User> users = userRepository.findAll();
        log.info("[expenditureGuideScheduler] 시작 - 대상 사용자: {}명", users.size());

        SchedulerResult result = new SchedulerResult();

        for (User user : users) {
            if (!isValidEmail(user.getUsername())) {
                log.warn("유효하지 않은 이메일 스킵: {}", user.getUsername());
                result.skip();
                continue;
            }

            try {
                ExpenditureGuideResponse response = expenditureService.expenditureGuide(user);
                sendEmail(user.getUsername(),
                        "📝 [Budget Management] 오늘의 지출 내역 안내",
                        "email/expenditure-guide",
                        createGuideContext(response));
                result.success();
                log.info("이메일 발송 성공: {}", user.getUsername());
            } catch (Exception e) {
                result.fail();
                log.error("이메일 발송 실패: {}", user.getUsername(), e);
            }
        }

        log.info("[expenditureGuideScheduler] 완료 - 성공: {}건, 실패: {}건, 스킵: {}건",
                result.successCount, result.failCount, result.skipCount);
    }

    private void sendEmail(String to, String subject, String template, Context context) throws MessagingException {
        String htmlContent = templateEngine.process(template, context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(FROM_EMAIL);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private Context createRecommendContext(ExpenditureRecommendResponse response) {
        Context context = new Context();
        context.setVariable("totalAmount", response.getTodayExpenditurePossibleTotal());
        context.setVariable("recommendList", response.getRecommendList());
        context.setVariable("message", response.getMessage());
        return context;
    }

    private Context createGuideContext(ExpenditureGuideResponse response) {
        Context context = new Context();
        context.setVariable("totalAmount", response.getTotalAmount());
        context.setVariable("guideList", response.getGuideList());
        return context;
    }

    private static class SchedulerResult {
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        void success() { successCount++; }
        void fail() { failCount++; }
        void skip() { skipCount++; }
    }
}