package ks.com.budgetmanagementproject.feature.expenditure.service;

import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureGuideResponse;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureRecommendResponse;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

@RequiredArgsConstructor
@Component
@EnableScheduling
@Slf4j
public class ExpenditureSchedulerService {

    private final ExpenditureService expenditureService;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    private static final Set<String> BLOCKED_DOMAINS = Set.of("example.com");

    private boolean deliverable(String email) {
        if (email == null) return false;
        String e = email.trim();
        if (e.isEmpty() || !e.contains("@")) return false;
        String domain = e.substring(e.lastIndexOf('@') + 1).toLowerCase();
        return !BLOCKED_DOMAINS.contains(domain);
    }

    @Transactional
    @Scheduled(cron = "0 26 15 * * ?", zone = "Asia/Seoul")
    public void expenditureRecommendScheduler() {
        List<User> users = userRepository.findAll();
        log.info("[expenditureRecommendScheduler] start: users={}", users.size());

        for (User user : users) {
            String to = user.getUsername();
            if (!deliverable(to)) {
                log.warn("[expenditureRecommendScheduler] skip invalid recipient: {}", to);
                continue;
            }
            try {
                ExpenditureRecommendResponse response = expenditureService.expenditureRecommend(user);

                // 본문 구성
                StringJoiner joiner = new StringJoiner("\n");
                if (response.getRecommendList() != null && !response.getRecommendList().isEmpty()) {
                    response.getRecommendList().forEach(rec ->
                            joiner.add(String.format("· %s: %,d원",
                                    rec.getCategory().getName(),
                                    rec.getTodayExpenditurePossibleMoney()))
                    );
                } else {
                    joiner.add("(추천 항목 없음: 예산 미설정 또는 이번 달 예산 초과)");
                }

                String text = "오늘의 카테고리별 지출 추천 금액:\n" + joiner +
                        "\n오늘 지출 총 가능 금액: " + String.format("%,d원", response.getTodayExpenditurePossibleTotal()) +
                        "\n메시지: " + response.getMessage();

                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom("pokj930@naver.com");      // 발신자(네이버)
                mailMessage.setTo(to);                          // 수신자(모든 유저)
                mailMessage.setSubject("안녕하세요! BudgetManagement 서비스 입니다. 오늘의 지출 금액을 추천해드려요!");
                mailMessage.setText(text);

                mailSender.send(mailMessage);
                log.info("[expenditureRecommendScheduler] sent to {}", to);
            } catch (Exception e) {
                log.error("[expenditureRecommendScheduler] mail send fail for {}", to, e);
            }
        }
        log.info("[expenditureRecommendScheduler] done");
    }

    @Transactional
    @Scheduled(cron = "0 0 20 * * ?", zone = "Asia/Seoul")
    public void expenditureGuideScheduler() {
        List<User> users = userRepository.findAll();
        log.info("[expenditureGuideScheduler] start: users={}", users.size());

        for (User user : users) {
            String to = user.getUsername();
            if (!deliverable(to)) {
                log.warn("[expenditureGuideScheduler] skip invalid recipient: {}", to);
                continue;
            }
            try {
                ExpenditureGuideResponse response = expenditureService.expenditureGuide(user);

                StringJoiner joiner = new StringJoiner("\n");
                if (response.getGuideList() != null && !response.getGuideList().isEmpty()) {
                    response.getGuideList().forEach(g ->
                            joiner.add(String.format("· %s | 오늘: %,d원 / 적정: %,d원 | 위험도: %s",
                                    g.getCategory().getName(),
                                    g.getTodayExpenditureAmount(),
                                    g.getTodayAppropriateExpenditureAmount(),
                                    g.getRisk()))
                    );
                } else {
                    joiner.add("(오늘 지출 내역 없음)");
                }

                String text = "오늘 카테고리별 지출 금액:\n" + joiner +
                        "\n오늘 총 지출 금액: " + String.format("%,d원", response.getTotalAmount());

                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom("pokj930@naver.com");  // 발신자 고정
                mailMessage.setTo(to);
                mailMessage.setSubject("안녕하세요! BudgetManagement 서비스 입니다. 오늘의 지출 내역을 안내해드려요!");
                mailMessage.setText(text);

                mailSender.send(mailMessage);
                log.info("[expenditureGuideScheduler] sent to {}", to);
            } catch (Exception e) {
                log.error("[expenditureGuideScheduler] mail send fail for {}", to, e);
            }
        }
        log.info("[expenditureGuideScheduler] done");
    }
}