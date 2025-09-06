package com.medwiz.novare_crm.repository;

import com.medwiz.novare_crm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByPhoneNumber(String phoneNumber);
    @Query("SELECT u FROM User u WHERE u.id = :id")
    User findUserById(@Param("id") String id);

}
