package seoultech.capstone.menjil.domain.chatbot.api.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chatbot.application.dto.request.ChatBotRoomServiceRequest;
import seoultech.capstone.menjil.domain.chatbot.domain.ChatBotRoom;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Getter
@NoArgsConstructor
public class ChatBotRoomRequest {

    @NotBlank
    private String initiatorNickname;
    @NotBlank
    private String recipientNickname;

    @Builder
    private ChatBotRoomRequest(String initiatorNickname, String recipientNickname) {
        this.initiatorNickname = initiatorNickname;
        this.recipientNickname = recipientNickname;
    }

    public static ChatBotRoomRequest fromChatBotRoom(ChatBotRoom room) {
        return ChatBotRoomRequest.builder()
                .initiatorNickname(room.getInitiatorNickname())
                .recipientNickname(room.getRecipientNickname())
                .build();
    }

    public ChatBotRoomServiceRequest toServiceRequest() {
        return ChatBotRoomServiceRequest.builder()
                .initiatorNickname(initiatorNickname)
                .recipientNickname(recipientNickname)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatBotRoomRequest roomDto = (ChatBotRoomRequest) o;
        return Objects.equals(initiatorNickname, roomDto.initiatorNickname)
                && Objects.equals(recipientNickname, roomDto.recipientNickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initiatorNickname, recipientNickname);
    }
}
