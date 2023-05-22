package seoultech.capstone.menjil.domain.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.user.dao.UserRepository;
import seoultech.capstone.menjil.domain.user.domain.User;
import seoultech.capstone.menjil.domain.user.dto.UserDto;
import seoultech.capstone.menjil.domain.user.dto.request.UserRequestDto;
import seoultech.capstone.menjil.domain.user.dto.response.UserSignupResponseDto;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import java.util.Map;

import static seoultech.capstone.menjil.global.common.JwtUtils.decodeJwt;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String JWT_SECRET_KEY;

    @Transactional(readOnly = true)
    public String checkNicknameDuplication(String nickname) {
        User user = userRepository.findUserByNickname(nickname).orElse(null);
        if (user != null) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }
        return "Nickname is available";
    }

    public UserSignupResponseDto signUp(UserRequestDto requestDto) {
        // UserRequestDto -> UserDto 변환 (decode jwt data)
        Map<String, Object> dataMap = decodeJwt(requestDto.getData());
        UserDto userDto = requestDto.toUserDto(dataMap);

        // UserDto -> User Entity 변환
        User user = userDto.toEntity();

        // db 에 저장
        try {
            User userInDb = userRepository.findUserByEmailAndProvider(user.getEmail(), user.getProvider())
                    .orElse(null);

            if (userInDb != null) {
                throw new CustomException(ErrorCode.USER_DUPLICATED);
            }

            userRepository.save(user);

        } catch (DataIntegrityViolationException e) {

            if (e.getCause() instanceof ConstraintViolationException) {
                if (e.getMessage().contains("users.email")) {
                    throw new CustomException(ErrorCode.USER_DUPLICATED);
                } else if (e.getMessage().contains("users.id")) {
                    throw new CustomException(ErrorCode.USER_DUPLICATED);
                }
            }
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }

        // User Entity -> UserSignupResponseDto
        return new UserSignupResponseDto(user);
    }
}
