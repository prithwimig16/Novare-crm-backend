package com.medwiz.novare_crm.service;

import com.medwiz.novare_crm.dto.request.DoctorUserRequest;
import com.medwiz.novare_crm.dto.request.UserRequest;
import com.medwiz.novare_crm.entity.Doctor;
import com.medwiz.novare_crm.entity.MemberProfile;
import com.medwiz.novare_crm.entity.User;
import com.medwiz.novare_crm.repository.DoctorRepository;
import com.medwiz.novare_crm.repository.MemberProfileRepository;
import com.medwiz.novare_crm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NovareRegistrationService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final MemberProfileRepository memberProfileRepository;

    @Transactional
    public String registerDoctor(DoctorUserRequest request, String keycloakUserId) {
        // Create User entity
        User user = User.builder()
                .id(keycloakUserId) // Keycloak sub
                .firstname(request.getFirstName())
                .lastname(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now()) // or use auditing
                .build();

        // Create Doctor entity
        Doctor doctor = Doctor.builder()
                .user(user) // link user
                .specialization(request.getSpecialization())
                .licenseNumber(request.getLicenseNumber())
                .department(request.getDepartment())
                .isActive(true)
                .build();

        doctorRepository.save(doctor);
        return "Doctor registered successfully";
    }

    @Transactional
    public String registerMember(UserRequest request, String keycloakUserId) {
        MemberProfile profile = MemberProfile.builder()
                .keycloakUserId(keycloakUserId)
                .age(request.getAge())
                .gender(request.getGender())
                .emergencyContact(request.getPhoneNumber())
                .build();

        memberProfileRepository.save(profile);

        User user = User.builder()
                .id(keycloakUserId) // same as Keycloak sub
                .firstname(request.getFirstName())
                .lastname(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .isVerified(false)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        return "Member registered successfully";
    }


    @Transactional
    public String registerUser(UserRequest request, String keycloakUserId) {

        // Save User metadata
        User user = User.builder()
                .id(keycloakUserId)
                .firstname(request.getFirstName())
                .lastname(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        return "Receptionist registered successfully";
    }

    @Transactional
    public String registerAdmin(UserRequest request, String keycloakUserId) {
        // Save User metadata
        User user = User.builder()
                .id(keycloakUserId)
                .firstname(request.getFirstName())
                .lastname(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        return "User registered successfully";
    }
}

