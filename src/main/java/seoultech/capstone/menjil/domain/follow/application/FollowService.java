package seoultech.capstone.menjil.domain.follow.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import seoultech.capstone.menjil.domain.follow.application.dto.request.FollowCreateServiceRequest;
import seoultech.capstone.menjil.domain.follow.dao.FollowRepository;
import seoultech.capstone.menjil.domain.follow.domain.Follow;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.Optional;

import static seoultech.capstone.menjil.global.exception.SuccessIntValue.FOLLOW_CREATED;
import static seoultech.capstone.menjil.global.exception.SuccessIntValue.FOLLOW_DELETED;

@Slf4j
@RequiredArgsConstructor
@Service
public class FollowService {

    private final FollowRepository followRepository;

    public int createFollow(FollowCreateServiceRequest request) {
        String userNickname = request.getUserNickname();
        String followNickname = request.getFollowNickname();

        // TODO: 이 부분에서, 조회할 때 실제 User가 있는지 확인이 필요하므로, 추후 User & Follow 연관관계 설정
        Optional<Follow> follow = followRepository.findFollowByUserNicknameAndFollowNickname(userNickname, followNickname);
        if (followIsExist(follow)) {
            // 팔로우가 존재하는 경우 팔로우 취소
            followRepository.delete(follow.get());
            return FOLLOW_DELETED.getValue();
        } else {
            // 팔로우가 존재하지 않는 경우 팔로우 등록
            Follow newfollow = Follow.of(userNickname, followNickname, LocalDateTime.now());
            saveFollow(newfollow);
            return FOLLOW_CREATED.getValue();
        }
    }

    public boolean checkFollowStatus(String userNickname, String followNickname) {
        Optional<Follow> follow = followRepository.findFollowByUserNicknameAndFollowNickname(userNickname, followNickname);
        return followIsExist(follow);
    }

    protected boolean followIsExist(Optional<Follow> follow) {
        return follow.isPresent();
    }

    private void saveFollow(Follow follow) {
        try {
            followRepository.save(follow);
        } catch (RuntimeException e) {
            log.error(">> Follow save exception occurred in: ", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
