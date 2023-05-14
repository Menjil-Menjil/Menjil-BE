package seoultech.capstone.menjil.domain.user.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.user.dao.UserRepository;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public String checkNicknameDuplication(String nickname) {
        boolean nicknameExistStatus = userRepository.existsByNickname(nickname);
        if (nicknameExistStatus)
            return "Nickname is available";
        else
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
    }
}
