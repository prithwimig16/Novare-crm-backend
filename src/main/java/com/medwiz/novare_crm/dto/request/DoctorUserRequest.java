package com.medwiz.novare_crm.dto.request;
import com.medwiz.novare_crm.enums.Specialization;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorUserRequest extends UserRequest {
    private Specialization specialization;
    private String licenseNumber;
    private String department;
}
