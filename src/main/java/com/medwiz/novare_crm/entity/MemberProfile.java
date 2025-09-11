package com.medwiz.novare_crm.entity;

import com.medwiz.novare_crm.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "member_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberProfile {
    @Id
    private String keycloakUserId;

    private int age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String phone;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RetreatRegistration> registrations;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keycloakUserId", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;
}
