package ks.com.budgetmanagementproject.feature.chat.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomResponse {

    @NotNull(message = "채팅방 ID는 필수입니다.")
    private Long id;

    @NotBlank(message = "채팅방 이름은 필수입니다.")
    @Size(max = 50, message = "채팅방 이름은 50자를 초과할 수 없습니다.")
    private String roomName;

    @NotBlank(message = "생성자 이름은 필수입니다.")
    private String creatorName;

    @NotNull(message = "멤버 수는 필수입니다.")
    @Min(value = 0, message = "멤버 수는 0 이상이어야 합니다.")
    private Integer memberCount;
}