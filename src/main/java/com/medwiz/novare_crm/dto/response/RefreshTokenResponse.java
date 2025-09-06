package com.medwiz.novare_crm.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class RefreshTokenResponse {
    private String access_token;
    private String refresh_token;
    private boolean sessionValid;

}

