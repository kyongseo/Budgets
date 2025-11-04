package ks.com.budgetmanagementproject.feature.expenditure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ks.com.budgetmanagementproject.feature.expenditure.dto.*;
import ks.com.budgetmanagementproject.feature.expenditure.service.ExpenditureService;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponse;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenditures")
@Tag(name = "Expenditure", description = "Expenditure API")
public class ExpenditureController {

    private final ExpenditureService expenditureService;

    @Operation(summary = "✅ 지출 생성",  description = "지출 생성")
    @PostMapping
    public ResponseEntity<?> expenditureCreate(@Validated @RequestBody ExpenditureCreateRequest request,
                                               @AuthenticationPrincipal User user) {
        expenditureService.expenditureCreate(request, user);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_CREATE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_CREATE_SUCCESS));
    }

    @Operation(summary = "✅ 지출 수정", description = "지출 수정")
    @PatchMapping("/{expenditureId}")
    public ResponseEntity<?> expenditureUpdate(@PathVariable Long expenditureId,
                                               @Validated @RequestBody ExpenditureUpdateRequest request,
                                               @AuthenticationPrincipal User user) {
        expenditureService.expenditureUpdate(expenditureId, request, user);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_UPDATE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_UPDATE_SUCCESS));
    }

    @Operation(summary = "✅ 지출 목록 조회", description = "지출 목록 조회")
    @GetMapping
    public ResponseEntity<?> expenditureList(@RequestParam LocalDate minPeriod, @RequestParam LocalDate maxPeriod,
                                             @RequestParam String categoryName, @RequestParam BigDecimal  minMoney,
                                             @RequestParam BigDecimal maxMoney, @AuthenticationPrincipal User user) {

        ExpenditureListResponse response = expenditureService.expenditureList(minPeriod, maxPeriod, categoryName, minMoney, maxMoney, user);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_LIST_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_LIST_SUCCESS, response));
    }

    @Operation(summary = "✅ 지출 상세 조회", description = "지출 상세 조회")
    @GetMapping("/{expenditureId}")
    public ResponseEntity<?> expenditureDetail(@PathVariable Long expenditureId,
                                               @AuthenticationPrincipal User user) {
        ExpenditureDetailResponse response = expenditureService.expenditureDetail(expenditureId, user);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_DETAIL_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_DETAIL_SUCCESS, response));
    }

    @Operation(summary = "✅ 지출 삭제(soft)", description = "지출 삭제(soft)")
    @DeleteMapping("/soft-delete/{expenditureId}")
    public ResponseEntity<?> expenditureSoftDelete(@PathVariable Long expenditureId,
                                                   @AuthenticationPrincipal User user) {
        expenditureService.expenditureSoftDelete(expenditureId, user);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_SOFT_DELETE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_SOFT_DELETE_SUCCESS));
    }

    @Operation(summary = "✅ 지출 삭제(hard)", description = "지출 삭제(hard)")
    @DeleteMapping("/hard-delete/{expenditureId}")
    public ResponseEntity<?> expenditureHardDelete(@PathVariable Long expenditureId,
                                                   @AuthenticationPrincipal User user) {
        expenditureService.expenditureHardDelete(expenditureId, user);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_HARD_DELETE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_HARD_DELETE_SUCCESS));
    }

    @Operation(summary = "✅ 지출 합계 제외 업데이트", description = "지출 합계 제외 업데이트")
    @PatchMapping("/except/{expenditureId}")
    public ResponseEntity<?> expenditureExceptUpdate(@PathVariable Long expenditureId,
                                                     @AuthenticationPrincipal User user,
                                                     @RequestParam boolean excludingTotal) {
        expenditureService.expenditureExceptUpdate(expenditureId, user, excludingTotal);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_EXCEPT_UPDATE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_EXCEPT_UPDATE_SUCCESS));
    }

    @Operation(summary = "✅ 지출 추천", description = "지출 추천")
    @GetMapping("/recommend")
    public ResponseEntity<?> expenditureRecommend(@AuthenticationPrincipal User user) {
        ExpenditureRecommendResponse response = expenditureService.expenditureRecommend(user);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_RECOMMEND_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_RECOMMEND_SUCCESS, response));
    }

    @Operation(summary = "✅ 지출 안내", description = "지출 안내")
    @GetMapping("/guide")
    public ResponseEntity<?> expenditureGuide(@AuthenticationPrincipal User user) {
        ExpenditureGuideResponse response = expenditureService.expenditureGuide(user);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_GUIDE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_GUIDE_SUCCESS, response));
    }
}