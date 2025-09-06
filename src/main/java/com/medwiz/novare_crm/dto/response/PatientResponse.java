package com.medwiz.novare_crm.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatientResponse {
    private UUID id;
    private String mrdNumber;
    private String fullName;
    private int age;
    private String gender;
    private String phone;
    private LocalDate registeredAt;
}
