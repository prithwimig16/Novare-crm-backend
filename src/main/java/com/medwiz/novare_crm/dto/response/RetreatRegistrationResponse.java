package com.medwiz.novare_crm.dto.response;
import com.medwiz.novare_crm.enums.Gender;
import com.medwiz.novare_crm.enums.Goal;
import com.medwiz.novare_crm.enums.PreferredMode;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RetreatRegistrationResponse(
        UUID id,
        String phone,
        Goal goal,
        String city,
        PreferredMode preferredMode,
        String additionalDetails,
        int age,
        Gender gender,
        String memberName,
        String memberEmail
) {}

