package com.medwiz.novare_crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.medwiz.novare_crm.enums.Currency;
import com.medwiz.novare_crm.enums.Specialization;
import jakarta.persistence.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Builder
@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private BigDecimal fees;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    private Integer experience;

    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Review> reviews;

    @Enumerated(EnumType.STRING)
    private Specialization specialization;

    private String licenseNumber;
    private String department;
    private boolean isActive;
}

