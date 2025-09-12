package com.medwiz.novare_crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username number is required")
    private String contact;

    @NotBlank(message = "Password is required")
    private String password;
}
