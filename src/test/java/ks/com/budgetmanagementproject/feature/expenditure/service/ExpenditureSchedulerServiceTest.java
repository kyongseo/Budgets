package ks.com.budgetmanagementproject.feature.expenditure.service;

import jakarta.mail.internet.MimeMessage;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureGuide;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureGuideResponse;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureRecommend;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureRecommendResponse;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenditureSchedulerServiceTest {

    @Mock
    ExpenditureService expenditureService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    ExpenditureSchedulerService expenditureSchedulerService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).username("testuser1@gmail.com").build();
        user2 = User.builder().id(2L).username("testuser2@naver.com").build();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("지출 추천 스케줄러_정상 실행 및 메일 발송")
    void expenditureRecommendScheduler_Success() throws Exception {
        // Given
        List<User> users = List.of(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        ExpenditureRecommendResponse response1 = new ExpenditureRecommendResponse(
                Collections.singletonList(new ExpenditureRecommend()), 100000L, "Good");
        ExpenditureRecommendResponse response2 = new ExpenditureRecommendResponse(
                Collections.singletonList(new ExpenditureRecommend()), 50000L, "Warning");

        when(expenditureService.expenditureRecommend(user1)).thenReturn(response1);
        when(expenditureService.expenditureRecommend(user2)).thenReturn(response2);

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Recommend Email Content</html>");

        // When
        expenditureSchedulerService.expenditureRecommendScheduler();

        // Then - 모든 사용자에게 메일 발송 확인
        verify(expenditureService).expenditureRecommend(user1);
        verify(expenditureService).expenditureRecommend(user2);
        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("지출 안내 스케줄러_정상 실행 및 메일 발송")
    void expenditureGuideScheduler_Success() throws Exception {
        // Given
        List<User> users = List.of(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        ExpenditureGuideResponse response1 = new ExpenditureGuideResponse(
                Collections.singletonList(new ExpenditureGuide()), new BigDecimal("150000"));
        ExpenditureGuideResponse response2 = new ExpenditureGuideResponse(
                Collections.singletonList(new ExpenditureGuide()), new BigDecimal("80000"));

        when(expenditureService.expenditureGuide(user1)).thenReturn(response1);
        when(expenditureService.expenditureGuide(user2)).thenReturn(response2);

        when(templateEngine.process(eq("email/expenditure-guide"), any(Context.class)))
                .thenReturn("<html>Guide Email Content</html>");

        // When
        expenditureSchedulerService.expenditureGuideScheduler();

        // Then - 모든 사용자에게 메일 발송 확인
        verify(expenditureService).expenditureGuide(user1);
        verify(expenditureService).expenditureGuide(user2);
        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }
}