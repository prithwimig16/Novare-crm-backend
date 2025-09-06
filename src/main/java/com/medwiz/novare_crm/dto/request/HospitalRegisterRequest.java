package com.medwiz.novare_crm.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class HospitalRegisterRequest {
    @NotBlank(message = "Hospital name is required")
    private String hospitalName;
    @Email(message = "Email should be valid")
    @NotBlank(message = "Contact email is required")
    private String contactName;
    @NotBlank(message = "State is required")
    private String state;
    private String district;
    private String city;
    private String pinCode;
    private String webUrl;
    private String status;
    private String contactEmail;
    private String phoneNumber;
    private String hospitalType;
    @Positive(message = "Capacity must be a positive number")
    private Integer capacity;
    @NotBlank(message = "License number is required")
    private String licenseNumber;
}
