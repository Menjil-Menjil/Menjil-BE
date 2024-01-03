package seoultech.capstone.menjil.domain.chatbot.api.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chatbot.application.dto.request.DeleteChatBotRoomServiceRequest;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class DeleteChatBotRoomRequest {

    @NotBlank
    private String initiatorNickname;

    @NotBlank
    private String recipientNickname;

    @NotBlank
    private String roomId;

    public DeleteChatBotRoomServiceRequest toServiceRequest() {
        return DeleteChatBotRoomServiceRequest.builder()
                .recipientNickname(recipientNickname)
                .initiatorNickname(initiatorNickname)
                .roomId(roomId)
                .build();
    }

    @Builder
    private DeleteChatBotRoomRequest(String initiatorNickname, String recipientNickname, String roomId) {
        this.initiatorNickname = initiatorNickname;
        this.recipientNickname = recipientNickname;
        this.roomId = roomId;
    }
}
