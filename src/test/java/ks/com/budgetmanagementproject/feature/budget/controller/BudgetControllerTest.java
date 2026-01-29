package ks.com.budgetmanagementproject.feature.budget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ks.com.budgetmanagementproject.feature.budget.dto.*;
import ks.com.budgetmanagementproject.feature.budget.service.BudgetService;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.global.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = BudgetController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BudgetService budgetService;

    @Nested
    @DisplayName("POST /budgets - 예산 설정")
    class BudgetSetting {

        @Test
        @DisplayName("예산_설정_성공")
        void budgetSettingSuccess() throws Exception {
            // given
            BudgetSettingRequest request = BudgetSettingRequest.builder()
                    .categoryName("식비")
                    .money(BigDecimal.valueOf(100_000L))
                    .period(YearMonth.of(2025, 9))
                    .build();

            doNothing().when(budgetService).createBudget(any(BudgetSettingRequest.class), any());

            mockMvc.perform(post("/budgets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated());

            verify(budgetService).createBudget(any(BudgetSettingRequest.class), any());
        }
    }

    @Nested
    @DisplayName("PATCH /budgets/{budgetId} - 예산 수정")
    class BudgetUpdate {

        @Test
        @DisplayName("예산_수정_성공")
        void budgetUpdateSuccess() throws Exception {
            // given
            Long budgetId = 1L;
            BudgetUpdateRequest request = BudgetUpdateRequest.builder()
                    .money(BigDecimal.valueOf(150_000L))
                    .build();

            when(budgetService.budgetUpdate(eq(budgetId), any()))
                    .thenReturn(null);

            // when
            mockMvc.perform(patch("/budgets/{budgetId}", budgetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            // then
            verify(budgetService).budgetUpdate(eq(budgetId), any(BudgetUpdateRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /budgets/soft-delete/{budgetId}")
    class BudgetSoftDelete {

        @Test
        @DisplayName("예산_소프트_삭제_성공")
        void budgetSoftDeleteSuccess() throws Exception {
            // given
            Long budgetId = 1L;
            doNothing().when(budgetService).budgetSoftDelete(budgetId);

            // when & then
            mockMvc.perform(delete("/budgets/soft-delete/{budgetId}", budgetId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            verify(budgetService).budgetSoftDelete(budgetId);
        }
    }

    @Nested
    @DisplayName("DELETE /budgets/hard-delete/{budgetId}")
    class BudgetHardDelete {

        @Test
        @DisplayName("예산_하드_삭제_성공")
        void budgetHardDeleteSuccess() throws Exception {
            // given
            Long budgetId = 1L;
            doNothing().when(budgetService).budgetHardDelete(budgetId);

            // when & then
            mockMvc.perform(delete("/budgets/hard-delete/{budgetId}", budgetId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            verify(budgetService).budgetHardDelete(budgetId);
        }
    }

    @Nested
    @DisplayName("GET /budgets/recommend")
    class BudgetRecommend {

        @Test
        @DisplayName("예산_추천_성공")
        void budgetRecommendSuccess() throws Exception {
            // given
            long totalAmount = 1_000_000L;
            BudgetCategoryResponse foodCategoryResponse = BudgetCategoryResponse.builder()
                    .id(1L)
                    .name("식비")
                    .build();

            BudgetRecommendResponse recommendResponse = BudgetRecommendResponse.builder()
                    .category(foodCategoryResponse)
                    .average(300_000L)
                    .build();

            BudgetRecommendListResponse response = new BudgetRecommendListResponse(
                    List.of(recommendResponse)
            );

            given(budgetService.budgetRecommend(totalAmount)).willReturn(response);

            // when & then
            mockMvc.perform(get("/budgets/recommend")
                            .param("totalAmount", String.valueOf(totalAmount)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.responseList").isArray())
                    .andExpect(jsonPath("$.result.responseList[0].category.name").value("식비"))
                    .andExpect(jsonPath("$.result.responseList[0].average").value(300_000L));

            verify(budgetService).budgetRecommend(totalAmount);
        }

        @Test
        @DisplayName("예산_추천_성공_빈_리스트")
        void budgetRecommendSuccessEmptyList() throws Exception {
            // given
            long totalAmount = 10_000L;
            BudgetRecommendListResponse response = new BudgetRecommendListResponse(List.of());

            given(budgetService.budgetRecommend(totalAmount)).willReturn(response);

            // when & then
            mockMvc.perform(get("/budgets/recommend")
                            .param("totalAmount", String.valueOf(totalAmount)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.responseList").isEmpty());

            verify(budgetService).budgetRecommend(totalAmount);
        }
    }
}