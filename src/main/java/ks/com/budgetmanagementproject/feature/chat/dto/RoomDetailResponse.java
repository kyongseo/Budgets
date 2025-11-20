package ks.com.budgetmanagementproject.feature.chat.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomDetailResponse {

    @NotNull(message = "채팅방 ID는 필수입니다.")
    private Long id;

    @NotBlank(message = "채팅방 이름은 필수입니다.")
    @Size(max = 50, message = "채팅방 이름은 50자를 초과할 수 없습니다.")
    private String roomName;

    @NotBlank(message = "생성자 이름은 필수입니다.")
    private String creatorName;

    @NotNull(message = "멤버 목록은 필수입니다.")
    @Valid
    private List<MemberInfo> members;
}
