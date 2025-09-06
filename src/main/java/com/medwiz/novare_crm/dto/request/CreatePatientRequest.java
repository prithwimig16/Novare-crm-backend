package com.medwiz.novare_crm.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePatientRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    private String gender;

    @Min(0)
    private int age;

    @NotBlank
    @Pattern(regexp = "^\\d{10}$", message = "Phone must be 10 digits")
    private String phone;
}

