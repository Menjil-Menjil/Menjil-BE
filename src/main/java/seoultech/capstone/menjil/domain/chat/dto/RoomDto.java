package seoultech.capstone.menjil.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chat.domain.Room;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {

    @NotBlank
    private String roomId;
    @NotBlank
    private String menteeNickname;
    @NotBlank
    private String mentorNickname;

    @Builder
    public Room toRoom() {
        return Room.builder()
                .roomId(roomId)
                .menteeNickname(menteeNickname)
                .mentorNickname(mentorNickname)
                .build();
    }

}
