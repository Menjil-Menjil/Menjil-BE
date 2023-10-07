package seoultech.capstone.menjil.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessIntValue {
    SUCCESS(0),

    // follow
    FOLLOW_CREATED(10),
    FOLLOW_DELETED(11);

    private final int value;
}
