package seoultech.capstone.menjil.domain.follow.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.follow.dao.Follow;
import seoultech.capstone.menjil.domain.follow.domain.FollowRepository;
import seoultech.capstone.menjil.domain.follow.dto.request.FollowRequest;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class FollowService {

    private final FollowRepository followRepository;

    public int followRequest(FollowRequest followRequest) {
        int INTERNAL_SERVER_ERROR = 1;
        int FOLLOW_CREATED = 100;
        int FOLLOW_DELETED = 101;
        String userNickname = followRequest.getUserNickname();
        String followNickname = followRequest.getFollowNickname();

        Optional<Follow> follow = followRepository.findFollowByUserNicknameAndFollowNickname(userNickname, followNickname);
        if (follow.isPresent()) {
            // 팔로우가 존재하는 경우 팔로우 취소
            followRepository.delete(follow.get());
            return FOLLOW_DELETED;
        } else {
            // 팔로우가 존재하지 않는 경우 팔로우 등록
            Follow newfollow = Follow.of(userNickname, followNickname, LocalDateTime.now());
            try {
                followRepository.save(newfollow);
            } catch (RuntimeException e) {
                log.error(">> save exception occurred in: ", e);
                return INTERNAL_SERVER_ERROR;
            }
            return FOLLOW_CREATED;
        }
    }
}
