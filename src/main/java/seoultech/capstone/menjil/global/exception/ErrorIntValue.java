package seoultech.capstone.menjil.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorIntValue {

    // auth
    USER_ALREADY_EXISTED(-1),

    // chat
    TIME_INPUT_INVALID(-11),


    // common
    INTERNAL_SERVER_ERROR(-100);

    private final int value;
}
