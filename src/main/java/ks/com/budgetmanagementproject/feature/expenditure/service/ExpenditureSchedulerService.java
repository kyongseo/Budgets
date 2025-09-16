package ks.com.budgetmanagementproject.feature.expenditure.service;

import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureGuideResponse;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureRecommendResponse;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ExpenditureSchedulerService {

    private final ExpenditureService expenditureService;

    private final UserRepository userRepository;

    private final JavaMailSender mailSender;

    @Transactional
    @Scheduled(cron = "0 0 8 * * ?", zone = "Asia/Seoul")
    public void expenditureRecommendScheduler() {
        List<User> users = userRepository.findAll();
        for (int i = 0; i < users.size(); i++) {
            String message = "오늘의 카테고리별 지출 추천 금액: ";
            User user = users.get(i);
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            ExpenditureRecommendResponse response = expenditureService.expenditureRecommend(user);

            mailMessage.setTo(user.getUsername());
            mailMessage.setSubject("안녕하세요! BudgetManagement 서비스 입니다. 오늘의 지출 금액을 추천해드려요!");
            for (int j = 0; j < response.getRecommendList().size(); j++) {
                message += " 카테고리: " + response.getRecommendList().get(j).getCategory().getName() +
                        " 지출 가능 금액: " + response.getRecommendList().get(j).getTodayExpenditurePossibleMoney();
            }
            mailMessage.setText(message + " 오늘 지출 총 가능 금액: " + response.getTodayExpenditurePossibleTotal() +  " 메시지: " + response.getMessage());
            mailSender.send(mailMessage);
        }

    }

    @Transactional
    @Scheduled(cron = "0 0 20 * * ?", zone = "Asia/Seoul")
    public void expenditureGuideScheduler() {
        List<User> users = userRepository.findAll();
        for (int i = 0; i < users.size(); i++) {
            String message = "오늘 카테고리별 지출 금액: ";
            User user = users.get(i);
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            ExpenditureGuideResponse response = expenditureService.expenditureGuide(user);
            mailMessage.setTo(user.getUsername());
            mailMessage.setSubject("안녕하세요! BudgetManagement 서비스 입니다. 오늘의 지출 내역을 안내해드려요!");
            for (int j = 0; j < response.getGuideList().size(); j++) {
                message += " 카테고리: " + response.getGuideList().get(j).getCategory().getName()  +
                        " 오늘 지출 금액: " + response.getGuideList().get(j).getTodayExpenditureAmount() +
                        " 오늘 적정 지출 금액: " + response.getGuideList().get(j).getTodayAppropriateExpenditureAmount() +
                        " 위험도: " + response.getGuideList().get(j).getRisk();
            }
            mailMessage.setText(message + " 오늘 총 지출 금액: " + response.getTotalAmount());
            mailSender.send(mailMessage);
        }
    }
}