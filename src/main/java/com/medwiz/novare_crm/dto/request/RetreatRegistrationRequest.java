package com.medwiz.novare_crm.dto.request;

import com.medwiz.novare_crm.enums.Gender;
import com.medwiz.novare_crm.enums.Goal;
import com.medwiz.novare_crm.enums.PreferredMode;

public record RetreatRegistrationRequest(
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        String password,
        int age,
        Gender gender,
        String emergencyContact,
        Goal goal,
        PreferredMode preferredMode,
        String additionalDetails
) {}

