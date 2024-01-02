package seoultech.capstone.menjil.domain.chatbot.api.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chatbot.domain.ChatBotRoom;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Getter
@NoArgsConstructor
public class ChatBotRoomDto {

    @NotBlank
    private String initiatorNickname;
    @NotBlank
    private String recipientNickname;

    @Builder
    private ChatBotRoomDto(String initiatorNickname, String recipientNickname) {
        this.initiatorNickname = initiatorNickname;
        this.recipientNickname = recipientNickname;
    }

    public static ChatBotRoomDto fromChatBotRoom(ChatBotRoom room) {
        return ChatBotRoomDto.builder()
                .initiatorNickname(room.getInitiatorNickname())
                .recipientNickname(room.getRecipientNickname())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatBotRoomDto roomDto = (ChatBotRoomDto) o;
        return Objects.equals(initiatorNickname, roomDto.initiatorNickname)
                && Objects.equals(recipientNickname, roomDto.recipientNickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initiatorNickname, recipientNickname);
    }
}
