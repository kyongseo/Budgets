package ks.com.budgetmanagementproject.feature.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequest {

    @NotBlank(message = "채팅방 이름은 필수입니다")
    @Size(min = 1, max = 50, message = "채팅방 이름은 1자 이상 50자 이하로 입력해주세요.")
    private String roomName;
}