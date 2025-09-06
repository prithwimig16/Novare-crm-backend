package com.medwiz.novare_crm.entity;
import com.medwiz.novare_crm.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private String id;// Keycloak "sub", a String

    private String firstname;
    private String lastname;
    private String phoneNumber;

    private boolean isVerified;
    private boolean isActive;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    private LocalDateTime createdAt;
}

