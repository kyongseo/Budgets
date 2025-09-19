package ks.com.budgetmanagementproject.feature.expenditure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ks.com.budgetmanagementproject.feature.expenditure.dto.*;
import ks.com.budgetmanagementproject.feature.expenditure.service.ExpenditureService;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenditures")
@Tag(name = "Expenditure", description = "Expenditure API")
public class ExpenditureController {

    private final ExpenditureService expenditureService;

    @Operation(summary = "✅ 지출 생성", responses = {
            @ApiResponse(responseCode = "201")
    })
    @PostMapping
    public ResponseEntity<?> expenditureCreate(@Validated @RequestBody ExpenditureCreateRequest request, @AuthenticationPrincipal User user) {
        expenditureService.expenditureCreate(request, user);

        return ResponseEntity.created(URI.create("/api/expenditures")).body(new BaseResponse<>(201, "지출 생성에 성공했습니다."));
    }

    @Operation(summary = "✅ 지출 수정", responses = {
            @ApiResponse(responseCode = "200")
    })
    @PatchMapping("/{expenditureId}")
    public ResponseEntity<?> expenditureUpdate(@PathVariable Long expenditureId, @Validated @RequestBody ExpenditureUpdateRequest request, @AuthenticationPrincipal User user) {
        expenditureService.expenditureUpdate(expenditureId, request, user);

        return ResponseEntity.ok().body(new BaseResponse<>(200, "지출 수정에 성공했습니다."));
    }

    @Operation(summary = "✅ 지출 목록 조회", responses = {
            @ApiResponse(responseCode = "200")
    })
    @GetMapping
    public ResponseEntity<?> expenditureList(@RequestParam LocalDate minPeriod, @RequestParam LocalDate maxPeriod,
                                             @RequestParam String categoryName, @RequestParam long minMoney,
                                             @RequestParam long maxMoney, @AuthenticationPrincipal User user) {
        ExpenditureListResponse listResponse = expenditureService.expenditureList(minPeriod, maxPeriod, categoryName, minMoney, maxMoney, user);

        return ResponseEntity.ok().body(new BaseResponse<>(200, "지출 목록 조회에 성공했습니다.", listResponse));
    }

    @Operation(summary = "✅ 지출 상세 조회", responses = {
            @ApiResponse(responseCode = "200")
    })
    @GetMapping("/{expenditureId}")
    public ResponseEntity<?> expenditureDetail(@PathVariable Long expenditureId, @AuthenticationPrincipal User user) {
        ExpenditureDetailResponse response = expenditureService.expenditureDetail(expenditureId, user);

        return ResponseEntity.ok().body(new BaseResponse<>(200, "지출 상세 조회에 성공했습니다.", response));
    }

    @Operation(summary = "✅ 지출 삭제(soft)", responses = {
            @ApiResponse(responseCode = "200")
    })
    @DeleteMapping("/soft-delete/{expenditureId}")
    public ResponseEntity<?> expenditureSoftDelete(@PathVariable Long expenditureId, @AuthenticationPrincipal User user) {
        expenditureService.expenditureSoftDelete(expenditureId, user);

        return ResponseEntity.ok().body(new BaseResponse<>(200, "지출 삭제에 성공했습니다."));
    }

    @Operation(summary = "✅ 지출 삭제(hard)", responses = {
            @ApiResponse(responseCode = "200")
    })
    @DeleteMapping("/hard-delete/{expenditureId}")
    public ResponseEntity<?> expenditureHardDelete(@PathVariable Long expenditureId, @AuthenticationPrincipal User user) {
        expenditureService.expenditureHardDelete(expenditureId, user);

        return ResponseEntity.ok().body(new BaseResponse<>(200, "지출 삭제에 성공했습니다."));
    }

    @Operation(summary = "✅ 지출 합계 제외 업데이트 API", responses = {
            @ApiResponse(responseCode = "200")
    })
    @PatchMapping("/except/{expenditureId}")
    public ResponseEntity<?> expenditureExceptUpdate(@PathVariable Long expenditureId, @AuthenticationPrincipal User user, @RequestParam boolean excludingTotal) {
        expenditureService.expenditureExceptUpdate(expenditureId, user, excludingTotal);

        return ResponseEntity.ok().body(new BaseResponse<>(200, "지출 합계 제외 업데이트에 성공했습니다."));
    }

    @Operation(summary = "✅ 지출 추천", responses = {
            @ApiResponse(responseCode = "200")
    })
    @GetMapping("/recommend")
    public ResponseEntity<?> expenditureRecommend(@AuthenticationPrincipal User user) {
        ExpenditureRecommendResponse response = expenditureService.expenditureRecommend(user);

        return ResponseEntity.ok().body(new BaseResponse<>(200, "지출 추천에 성공했습니다.", response));
    }

    @Operation(summary = "✅ 지출 안내", responses = {
            @ApiResponse(responseCode = "200")
    })
    @GetMapping("/guide")
    public ResponseEntity<?> expenditureGuide(@AuthenticationPrincipal User user) {
        ExpenditureGuideResponse response = expenditureService.expenditureGuide(user);

        return ResponseEntity.ok().body(new BaseResponse<>(200, "지출 안내에 성공했습니다.", response));
    }
}