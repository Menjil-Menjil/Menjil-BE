package seoultech.capstone.menjil.domain.auth.jwt;

public enum TokenStatus {
    EXPIRED,
    USER_ID_NOT_EXIST,
    REFRESH_TOKEN_NOT_EXIST,
    OTHER_EXCEPTION,
    RELIABLE
}
