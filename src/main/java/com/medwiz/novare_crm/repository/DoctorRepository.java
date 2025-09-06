package com.medwiz.novare_crm.repository;
import com.medwiz.novare_crm.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    Optional<Doctor> findByUserId(String userId);


    boolean existsByLicenseNumber(String licenseNumber);
}

