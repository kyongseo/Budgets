package ks.com.budgetmanagementproject.feature.expenditure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ks.com.budgetmanagementproject.feature.expenditure.dto.*;
import ks.com.budgetmanagementproject.feature.expenditure.service.ExpenditureService;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponse;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponseStatus;
import ks.com.budgetmanagementproject.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenditures")
@Tag(name = "Expenditure", description = "Expenditure API")
public class ExpenditureController {

    private final ExpenditureService expenditureService;

    @Operation(summary = "✅ 지출 생성",  description = "새로운 지출 내역을 등록합니다. 날짜, 금액, 카테고리, 메모 등을 포함할 수 있습니다.")
    @PostMapping
    public ResponseEntity<?> expenditureCreate_endpoint(
            @Validated @RequestBody ExpenditureCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String username = userDetails.getUsername();
        expenditureService.createExpenditure(request, username);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_CREATE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_CREATE_SUCCESS));
    }

    @Operation(summary = "✅ 지출 수정", description = "기존 지출 내역의 정보를 수정합니다. 본인의 지출만 수정 가능합니다.")
    @PatchMapping("/{expenditureId}")
    public ResponseEntity<?> expenditureUpdate_endpoint(
            @PathVariable Long expenditureId,
            @Validated @RequestBody ExpenditureUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String username = userDetails.getUsername();
        expenditureService.updateExpenditure(expenditureId, request, username);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_UPDATE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_UPDATE_SUCCESS));
    }

    @Operation(summary = "✅ 지출 목록 조회", description = "기간, 카테고리, 최소/최대 금액 등의 조건으로 지출 내역을 조회합니다. " +
            "합계 제외 설정된 지출은 필터링 옵션에 따라 조회 여부가 결정됩니다.")
    @GetMapping
    public ResponseEntity<?> expenditureList_endpoint(
            @Validated @ModelAttribute ExpenditureListRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String username = userDetails.getUsername();
        ExpenditureListResponse response = expenditureService.getExpenditureList(request, username);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_LIST_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_LIST_SUCCESS, response));
    }

    @Operation(summary = "✅ 지출 상세 조회", description = "지출 상세 조회")
    @GetMapping("/{expenditureId}")
    public ResponseEntity<?> expenditureDetail(
            @PathVariable Long expenditureId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String username = userDetails.getUsername();
        ExpenditureDetailResponse response = expenditureService.getExpenditureDetail(expenditureId, username);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_DETAIL_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_DETAIL_SUCCESS, response));
    }

    @Operation(summary = "✅ 지출 삭제(soft)", description = "지출 삭제(soft)")
    @DeleteMapping("/soft-delete/{expenditureId}")
    public ResponseEntity<?> expenditureSoftDelete(
            @PathVariable Long expenditureId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String username = userDetails.getUsername();
        expenditureService.softDeleteExpenditure(expenditureId, username);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_SOFT_DELETE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_SOFT_DELETE_SUCCESS));
    }

    @Operation(summary = "✅ 지출 삭제(hard)", description = "지출 삭제(hard)")
    @DeleteMapping("/hard-delete/{expenditureId}")
    public ResponseEntity<?> expenditureHardDelete(
            @PathVariable Long expenditureId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String username = userDetails.getUsername();
        expenditureService.hardDeleteExpenditure(expenditureId, username);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_HARD_DELETE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_HARD_DELETE_SUCCESS));
    }

    @Operation(summary = "✅ 지출 합계 제외 업데이트", description = "특정 지출 내역을 총 합계 계산에서 제외시킵니다.")
    @PatchMapping("/except/{expenditureId}")
    public ResponseEntity<?> expenditureExceptUpdate(
            @PathVariable Long expenditureId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Validated @RequestBody ExpenditureExcludeRequest request) {

        String username = userDetails.getUsername();
        expenditureService.updateExpenditureExclude(expenditureId, username, request);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_EXCEPT_UPDATE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_EXCEPT_UPDATE_SUCCESS));
    }

    @Operation(summary = "✅ 지출 추천", description = "사용자의 예산과 남은 기간을 고려하여 오늘 적정 지출 금액을 카테고리별로 추천합니다.")
    @GetMapping("/recommend")
    public ResponseEntity<?> expenditureRecommend(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String username = userDetails.getUsername();
        ExpenditureRecommendResponse response = expenditureService.getExpenditureRecommendation(username);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_RECOMMEND_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_RECOMMEND_SUCCESS, response));
    }

    @Operation(summary = "✅ 지출 안내", description = "오늘 사용한 지출과 적정 금액을 비교하여 분석 결과를 제공합니다.")
    @GetMapping("/guide")
    public ResponseEntity<?> expenditureGuide(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String username = userDetails.getUsername();
        ExpenditureGuideResponse response = expenditureService.getExpenditureGuide(username);

        return ResponseEntity
                .status(BaseResponseStatus.EXPENDITURE_GUIDE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.EXPENDITURE_GUIDE_SUCCESS, response));
    }
}