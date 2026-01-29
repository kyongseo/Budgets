package ks.com.budgetmanagementproject.feature.expenditure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ks.com.budgetmanagementproject.feature.expenditure.dto.*;
import ks.com.budgetmanagementproject.feature.expenditure.service.ExpenditureService;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExpenditureController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ExpenditureControllerTest {

    private static final Long DEFAULT_EXPENDITURE_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenditureService expenditureService;

    @Nested
    @DisplayName("POST /expenditures - 지출 생성")
    class ExpenditureCreate {

        @Test
        @DisplayName("지출_생성_성공")
        @WithMockUser
        void expenditureCreateSuccess() throws Exception {
            // given
            ExpenditureCreateRequest request = ExpenditureCreateRequest.builder()
                    .money(new BigDecimal("15000"))
                    .categoryName("식비")
                    .period(LocalDate.now())
                    .memo("점심값")
                    .build();

            doNothing().when(expenditureService).createExpenditure(any(ExpenditureCreateRequest.class), any());

            // when & then
            mockMvc.perform(post("/expenditures")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.message").exists());

            verify(expenditureService).createExpenditure(any(ExpenditureCreateRequest.class), any());
        }
    }

    @Nested
    @DisplayName("PATCH /expenditures/{id} - 지출 수정")
    class ExpenditureUpdate {

        @Test
        @DisplayName("지출_수정_성공")
        void expenditureUpdateSuccess() throws Exception {
            // given
            ExpenditureUpdateRequest request = ExpenditureUpdateRequest.builder()
                    .money(new BigDecimal("20000"))
                    .categoryName("교통")
                    .period(LocalDate.now())
                    .memo("버스 탑승")
                    .build();

            doNothing().when(expenditureService).updateExpenditure(eq(DEFAULT_EXPENDITURE_ID), any(ExpenditureUpdateRequest.class), any());

            // when & then
            mockMvc.perform(patch("/expenditures/{id}", DEFAULT_EXPENDITURE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            verify(expenditureService).updateExpenditure(eq(DEFAULT_EXPENDITURE_ID), any(ExpenditureUpdateRequest.class), any());
        }
    }

    @Nested
    @DisplayName("GET /expenditures - 지출 목록 조회")
    class ExpenditureList {

        @Test
        @DisplayName("지출_목록_조회_성공")
        void expenditureListSuccess() throws Exception {

            // given
            LocalDate minPeriod = LocalDate.now().minusDays(7);
            LocalDate maxPeriod = LocalDate.now().minusDays(1);

            ExpenditureListResponseItem item1 = ExpenditureListResponseItem.builder()
                    .expenditureId(1L)
                    .money(15000)
                    .categoryName("식비")
                    .period(minPeriod)
                    .memo("점심값")
                    .excludingTotal(false)
                    .build();

            ExpenditureListResponseItem item2 = ExpenditureListResponseItem.builder()
                    .expenditureId(2L)
                    .money(20000)
                    .categoryName("교통")
                    .period(maxPeriod)
                    .memo("택시비")
                    .excludingTotal(false)
                    .build();

            List<ExpenditureListResponseItem> expenditureList = List.of(item1, item2);

            ExpenditureListResponse response = ExpenditureListResponse.builder()
                    .expenditureLists(expenditureList)
                    .viewMoneyTotal(35000L)
                    .totalCategoryMoneyTotal(35000L)
                    .build();

            given(expenditureService.getExpenditureList(
                    argThat(req -> req.getMinPeriod().equals(minPeriod)
                            && req.getMaxPeriod().equals(maxPeriod)),
                    eq("testUser")
            )).willReturn(response);

            // when & then
            mockMvc.perform(get("/expenditures")
                            .param("minPeriod", minPeriod.toString())
                            .param("maxPeriod", maxPeriod.toString())
                            .with(user("testUser"))) // 중요
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(BaseResponseStatus.EXPENDITURE_LIST_SUCCESS.getStatus().value()))
                    .andExpect(jsonPath("$.message").value(BaseResponseStatus.EXPENDITURE_LIST_SUCCESS.getMessage()));
        }
    }

    @Nested
    @DisplayName("GET /expenditures/{id} - 지출 상세 조회")
    class ExpenditureDetail {

        @Test
        @DisplayName("지출_상세_조회_성공")
        void expenditureDetailSuccess() throws Exception {
            // given
            ExpenditureDetailResponse response = new ExpenditureDetailResponse();
            given(expenditureService.getExpenditureDetail(eq(DEFAULT_EXPENDITURE_ID), any())).willReturn(response);

            // when & then
            mockMvc.perform(get("/expenditures/{id}", DEFAULT_EXPENDITURE_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            verify(expenditureService).getExpenditureDetail(eq(DEFAULT_EXPENDITURE_ID), any());
        }
    }

    @Nested
    @DisplayName("DELETE /expenditures/soft-delete/{id} - 소프트 삭제")
    class ExpenditureSoftDelete {

        @Test
        @DisplayName("지출_소프트_삭제_성공")
        void expenditureSoftDeleteSuccess() throws Exception {
            // given
            doNothing().when(expenditureService).softDeleteExpenditure(eq(DEFAULT_EXPENDITURE_ID), any());

            // when & then
            mockMvc.perform(delete("/expenditures/soft-delete/{id}", DEFAULT_EXPENDITURE_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            verify(expenditureService).softDeleteExpenditure(eq(DEFAULT_EXPENDITURE_ID), any());
        }
    }

    @Nested
    @DisplayName("DELETE /expenditures/hard-delete/{id} - 하드 삭제")
    class ExpenditureHardDelete {

        @Test
        @DisplayName("지출_하드_삭제_성공")
        void expenditureHardDeleteSuccess() throws Exception {
            // given
            doNothing().when(expenditureService).hardDeleteExpenditure(eq(DEFAULT_EXPENDITURE_ID), any());

            // when & then
            mockMvc.perform(delete("/expenditures/hard-delete/{id}", DEFAULT_EXPENDITURE_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            verify(expenditureService).hardDeleteExpenditure(eq(DEFAULT_EXPENDITURE_ID), any());
        }
    }

    @Nested
    @DisplayName("PATCH /expenditures/except/{id} - 합계 제외 업데이트")
    class ExpenditureExcludeUpdate {

        @Test
        @DisplayName("지출_합계_제외_업데이트_성공")
        void expenditureExcludeUpdateSuccess() throws Exception {
            // given
            ExpenditureExcludeRequest request = new ExpenditureExcludeRequest(true);

            doNothing().when(expenditureService).updateExpenditureExclude(eq(DEFAULT_EXPENDITURE_ID), any(), any());

            // when & then
            mockMvc.perform(patch("/expenditures/except/{id}", DEFAULT_EXPENDITURE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            verify(expenditureService).updateExpenditureExclude(eq(DEFAULT_EXPENDITURE_ID), any(), any());
        }
    }

    @Nested
    @DisplayName("GET /expenditures/recommend - 지출 추천")
    class ExpenditureRecommend {

        @Test
        @DisplayName("지출_추천_성공")
        void expenditureRecommendSuccess() throws Exception {
            // given
            ExpenditureRecommendResponse response = new ExpenditureRecommendResponse();
            given(expenditureService.getExpenditureRecommendation(any())).willReturn(response);

            // when & then
            mockMvc.perform(get("/expenditures/recommend"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            verify(expenditureService).getExpenditureRecommendation(any());
        }
    }

    @Nested
    @DisplayName("GET /expenditures/guide - 지출 안내")
    class ExpenditureGuide {

        @Test
        @DisplayName("지출_안내_성공")
        void expenditureGuideSuccess() throws Exception {
            // given
            ExpenditureGuideResponse response = new ExpenditureGuideResponse();
            given(expenditureService.getExpenditureGuide(any())).willReturn(response);

            // when & then
            mockMvc.perform(get("/expenditures/guide"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            verify(expenditureService).getExpenditureGuide(any());
        }
    }
}