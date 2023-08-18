package seoultech.capstone.menjil.domain.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoomInfoDto {

    private String roomId;
    private String nickname;
    private String imgUrl;
    private String lastMessage;

    private Long lastMessagedTimeOfHour;

    @Builder
    private RoomInfoDto(String roomId, String nickname, String imgUrl, String lastMessage,
                        Long lastMessagedTimeOfHour) {
        this.roomId = roomId;
        this.nickname = nickname;
        this.imgUrl = imgUrl;
        this.lastMessage = lastMessage;
        this.lastMessagedTimeOfHour = lastMessagedTimeOfHour;
    }
}
