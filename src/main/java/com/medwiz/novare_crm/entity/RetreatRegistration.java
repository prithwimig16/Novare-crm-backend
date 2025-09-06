package com.medwiz.novare_crm.entity;

import com.medwiz.novare_crm.enums.Gender;
import com.medwiz.novare_crm.enums.Goal;
import com.medwiz.novare_crm.enums.Mode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetreatRegistration {
    @Id
    @GeneratedValue
    private UUID id;

    private String keycloakUserId;

    @Enumerated(EnumType.STRING)
    private Goal goal;

    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "keycloakUserId")
    private MemberProfile member;

    @Enumerated(EnumType.STRING)
    private Mode preferredMode;

    @Column(length = 1000)
    private String additionalDetails;

    private int age;

    @Enumerated(EnumType.STRING)
    private Gender gender;
}
