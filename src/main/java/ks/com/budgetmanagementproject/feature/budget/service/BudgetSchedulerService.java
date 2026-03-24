package ks.com.budgetmanagementproject.feature.budget.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetSchedulerService {

    private final BudgetService budgetService;

    /**
     * 매일 새벽 02:00에 예산 추천 통계 캐시 갱신
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    public void refreshBudgetStatistics() {
        log.info(">>>> [BudgetScheduler] 통계 캐시 갱신 작업을 시작합니다.");
        try {
            budgetService.calculateAndCacheRatios();
            log.info(">>>> [BudgetScheduler] 통계 캐시 갱신 작업이 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            log.error(">>>> [BudgetScheduler] 통계 캐시 갱신 중 오류 발생: {}", e.getMessage());
        }
    }
}
