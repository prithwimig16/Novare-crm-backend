package com.medwiz.novare_crm.dto.request;

import com.medwiz.novare_crm.enums.Gender;
import com.medwiz.novare_crm.enums.Role;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    @jakarta.validation.constraints.Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private Gender gender;

    @NotNull
    private Role role;

    @Min(0)
    private int age;


    private String hospitalId;

}

