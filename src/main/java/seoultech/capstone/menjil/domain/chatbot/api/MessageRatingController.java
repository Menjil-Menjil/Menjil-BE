package seoultech.capstone.menjil.domain.chatbot.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seoultech.capstone.menjil.domain.chatbot.application.MessageRatingService;
import seoultech.capstone.menjil.domain.chatbot.dto.request.MessageClickIncViewsAndLikesRequest;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.exception.SuccessCode;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat/ratings")
public class MessageRatingController {

    private final MessageRatingService messageRatingService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> incrementViewsAndLikes(@RequestBody MessageClickIncViewsAndLikesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.MESSAGE_RATING_SUCCESS,
                        messageRatingService.incrementViewsAndLikes(request)));
    }
}
