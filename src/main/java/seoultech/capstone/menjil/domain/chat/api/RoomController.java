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
     * 채팅방을 입장한다
     */
    @PostMapping("/room/enter")
    public void enterTheRoom(@RequestBody RoomDto roomDto) {
        List<MessageResponse> messageList = roomService.enterTheRoom(roomDto);

        ResponseEntity<ApiResponse<List<MessageResponse>>> messageResponse;
        if (messageList.size() != 1) {
            messageResponse = ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(SuccessCode.MESSAGE_LOAD_SUCCESS, messageList));
        } else {
            if (messageList.get(0).getOrder() == null) {
                // This case is when the user enters the room at the first time.
                messageResponse = ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.success(SuccessCode.MESSAGE_CREATED, messageList));
            } else {
                messageResponse = ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(SuccessCode.MESSAGE_LOAD_SUCCESS, messageList));
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

        if (result.size() == 0) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success(SuccessCode.GET_ROOMS_AND_NOT_EXISTS, result));
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success(SuccessCode.GET_ROOMS_AVAILABLE, result));
        }
    }

    /**
     * 사용자가 방에서 퇴장한다
     */
    public void quitRoom() {

    }

}
