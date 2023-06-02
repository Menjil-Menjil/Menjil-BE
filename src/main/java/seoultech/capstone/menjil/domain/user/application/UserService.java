package seoultech.capstone.menjil.domain.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.user.dao.UserRepository;
import seoultech.capstone.menjil.domain.user.domain.User;
import seoultech.capstone.menjil.domain.user.dto.UserDto;
import seoultech.capstone.menjil.domain.user.dto.request.UserRequestDto;
import seoultech.capstone.menjil.domain.user.dto.response.UserSignupResponseDto;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import java.util.List;
import java.util.Map;

import static seoultech.capstone.menjil.global.common.JwtUtils.decodeJwt;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public String checkNicknameDuplication(String nickname) {
        List<User> NicknameExistsInDb = userRepository.findUserByNickname(nickname);
        if (NicknameExistsInDb.size() > 0) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }
        return "Nickname is available";
    }

    @Transactional
    public UserSignupResponseDto signUp(UserRequestDto requestDto) {
        // UserRequestDto -> UserDto 변환 (decode jwt data)
        Map<String, Object> dataMap = decodeJwt(requestDto.getData());
        UserDto userDto = requestDto.toUserDto(dataMap);

        // UserDto -> User Entity 변환
        User user = userDto.toEntity();

        // 기존에 중복된 유저가 있는 지 조회
        List<User> userInDb = userRepository.findUserByEmailAndNameAndProvider(user.getEmail(),
                user.getName(), user.getProvider());
        List<User> NicknameExistsInDb = userRepository.findUserByNickname(user.getNickname());

        if (userInDb.size() > 0) {
            throw new CustomException(ErrorCode.USER_DUPLICATED);
        }
        if (NicknameExistsInDb.size() > 0) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }

        // db에 저장
        userRepository.save(user);

        // User Entity -> UserSignupResponseDto
        return new UserSignupResponseDto(user);
    }
}
