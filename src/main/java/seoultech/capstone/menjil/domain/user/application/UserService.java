package seoultech.capstone.menjil.domain.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.user.dao.UserRepository;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public String checkNicknameDuplication(String nickname) {
        boolean nicknameExistStatus;

        try {
            nicknameExistStatus = userRepository.existsByNickname(nickname);
        } catch (Exception e) {
            log.error(">> DB error in find nickname :: ", e);
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }

        if (nicknameExistStatus)  // nickname 이 이미 존재하면 true
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        else
            return "Nickname is available";
    }
}
