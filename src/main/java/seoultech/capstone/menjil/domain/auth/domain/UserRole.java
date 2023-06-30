package seoultech.capstone.menjil.domain.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    MENTOR("ROLE_MENTOR"),
    MENTEE("ROLE_MENTEE");

    private final String role;

}
