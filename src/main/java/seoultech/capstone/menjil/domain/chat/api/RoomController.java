package seoultech.capstone.menjil.domain.chat.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import seoultech.capstone.menjil.domain.chat.application.RoomService;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.domain.chat.dto.response.MessageResponse;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfoResponse;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@EnableWebMvc
@RequestMapping("/api/chat")
public class RoomController {

    private final RoomService roomService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 채팅방에 입장한다
     */
    @PostMapping("/room/enter")
    public void enterTheRoom(@RequestBody RoomDto roomDto) {
        List<MessageResponse> messageList = roomService.enterTheRoom(roomDto);

        ApiResponse<List<MessageResponse>> messageResponse;
        if (chatMessageIsMoreThanOne(messageList)) {
            messageResponse = ApiResponse.success(SuccessCode.MESSAGE_LOAD_SUCCESS, messageList);
        } else {
            // 채팅 메시지가 하나인 경우
            if (checkIfUserEnterTheRoomAtFirstTime(messageList)) {
                messageResponse = ApiResponse.success(SuccessCode.MESSAGE_CREATED, messageList);
            } else {
                messageResponse = ApiResponse.success(SuccessCode.MESSAGE_LOAD_SUCCESS, messageList);
            }
        }

        // /queue/chat/room/{room id}로 메세지 보냄
        simpMessagingTemplate.convertAndSend("/queue/chat/room/" + roomDto.getRoomId(), messageResponse);
    }

    /**
     * 사용자의 전체 Room 목록을 불러온다
     */
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<RoomInfoResponse>>> getAllRoomsOfUser(@RequestParam("nickname") String nickname,
                                                                                 @RequestParam("type") String type) {
        List<RoomInfoResponse> result = roomService.getAllRoomsOfUser(nickname, type);

        if (userHasNoRooms(result)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success(SuccessCode.GET_ROOMS_AND_NOT_EXISTS, result));
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success(SuccessCode.GET_ROOMS_AVAILABLE, result));
        }
    }

    protected boolean chatMessageIsMoreThanOne(List<MessageResponse> messages) {
        int MESSAGE_IS_MORE_THAN_ONE = 1;
        return messages.size() > MESSAGE_IS_MORE_THAN_ONE;
    }

    protected boolean checkIfUserEnterTheRoomAtFirstTime(List<MessageResponse> messages) {
        return messages.get(0).getOrder() == null;
    }

    protected boolean userHasNoRooms(List<RoomInfoResponse> list) {
        return list.isEmpty();
    }
}
