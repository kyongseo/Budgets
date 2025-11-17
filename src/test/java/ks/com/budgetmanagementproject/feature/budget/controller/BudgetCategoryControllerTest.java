package ks.com.budgetmanagementproject.feature.budget.controller;

import ks.com.budgetmanagementproject.feature.budget.dto.BudgetCategoryResponse;
import ks.com.budgetmanagementproject.feature.budget.service.BudgetCategoryService;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BudgetCategoryController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
class BudgetCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BudgetCategoryService categoryService;

    @Nested
    @DisplayName("GET /budget/categories - 카테고리 목록 조회")
    class CategoryList {

        @Test
        @DisplayName("카테고리_목록_조회_성공")
        void categoryListSuccess() throws Exception {
            // given
            BudgetCategoryResponse food = BudgetCategoryResponse.builder()
                    .id(1L)
                    .name("식비")
                    .build();

            BudgetCategoryResponse trans = BudgetCategoryResponse.builder()
                    .id(2L)
                    .name("교통")
                    .build();

            List<BudgetCategoryResponse> responseList = List.of(food, trans);

            given(categoryService.categoryList()).willReturn(responseList);

            // when & then
            mockMvc.perform(get("/budget/categories"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(BaseResponseStatus.BUDGET_CATEGORY_LIST_SUCCESS.getStatus().value()))
                    .andExpect(jsonPath("$.message").value(BaseResponseStatus.BUDGET_CATEGORY_LIST_SUCCESS.getMessage()))
                    .andExpect(jsonPath("$.result[0].name").value("식비"))
                    .andExpect(jsonPath("$.result.length()").value(2));

            verify(categoryService).categoryList();
        }

        @Test
        @DisplayName("카테고리_목록_조회_성공_빈_리스트")
        void categoryListSuccessEmpty() throws Exception {
            // given
            given(categoryService.categoryList()).willReturn(Collections.emptyList());

            // when & then
            mockMvc.perform(get("/budget/categories"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.length()").value(0));

            verify(categoryService).categoryList();
        }
    }
}