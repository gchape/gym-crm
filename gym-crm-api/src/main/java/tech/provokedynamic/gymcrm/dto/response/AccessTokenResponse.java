package tech.provokedynamic.gymcrm.dto.response;



/** Signed bearer token returned by the login endpoint. */
public record AccessTokenResponse(
        String accessToken
) {
}
