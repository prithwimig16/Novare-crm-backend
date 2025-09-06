package com.medwiz.novare_crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.medwiz.novare_crm.config.AppConstraints;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = AppConstraints.ForAddress.MAX_ADDRESS1_LENGTH)
    private String address1;

    @Column(length = AppConstraints.ForAddress.MAX_ADDRESS2_LENGTH)
    private String address2;

    @Column(length = AppConstraints.ForAddress.MAX_CITY_LENGTH)
    private String city;

    @Column(length = AppConstraints.ForAddress.MAX_STATE_LENGTH)
    private String state;

    @Column(length = AppConstraints.ForAddress.MAX_ZIP_LENGTH)
    private String zip;

    @Column(length = AppConstraints.ForAddress.MAX_COUNTRY_LENGTH)
    private String country;

    private double latitude;

    private double longitude;

    @JsonIgnore
    @OneToOne(mappedBy = "address")
    private User user;



}
