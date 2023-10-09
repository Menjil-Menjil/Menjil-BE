package seoultech.capstone.menjil.domain.chat.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class MessageControllerSpringBootTest {

    @MockBean
    SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    MessageController messageController;

    @AfterEach
    void tearDown() {
    }

    /**
     * sendErrorResponse
     */
    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("ArgumentCaptor를 사용한 sendErrorResponse 테스트")
    void sendErrorResponse() {
        // given
        String roomId = "test_room_id";
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        ArgumentCaptor<ApiResponse<?>> argumentCaptor = ArgumentCaptor.forClass(ApiResponse.class);

        // when
        messageController.sendErrorResponse(roomId, errorCode);

        // then
        verify(simpMessagingTemplate).convertAndSend(eq("/queue/chat/room/" + roomId), argumentCaptor.capture());
        ApiResponse<?> capturedResponse = argumentCaptor.getValue();

        // Now manually check if the captured response has the fields you expect.
        assertThat(errorCode.getHttpStatus().value()).isEqualTo(capturedResponse.getCode());
        assertThat(errorCode.getMessage()).isEqualTo(capturedResponse.getMessage());
        assertThat(capturedResponse.getData()).isNull();
    }

}