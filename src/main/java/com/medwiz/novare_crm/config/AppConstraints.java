package com.medwiz.novare_crm.config;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AppConstraints {

    public class ForDoctor {
        public static final int MAX_ABOUT_LENGTH = 128;
        public static final int MAX_AGE_LENGTH = 3;
        public static final int MAX_PIN_CODE_LENGTH = 6;
        public static final int MAX_SPECIALIZATION_LENGTH = 64;
    }

    public class ForAddress {
        public static final int MAX_ADDRESS1_LENGTH = 128;
        public static final int MAX_ADDRESS2_LENGTH = 128;
        public static final int MAX_CITY_LENGTH = 32;
        public static final int MAX_STATE_LENGTH = 32;
        public static final int MAX_ZIP_LENGTH = 32;
        public static final int MAX_COUNTRY_LENGTH = 32;
    }

    public class ForBrand {
        public static final int MAX_BRAND_NAME_LENGTH = 64;
    }

    public class ForPrescription {
        public static final int MAX_PATIENT_HISTORY_LENGTH = 128;
    }

    public class ForDrug {
        public static final int MAX_DRUG_NAME_LENGTH = 32;
        public static final int MAX_GENRE_NAME_LENGTH = 32;
        public static final int MAX_DOSAGE_LENGTH = 32;
        public static final int MAX_INDICATION_LENGTH = 64;
        public static final int MAX_COMPOSITION_LENGTH = 64;
        public static final int MAX_USAGE_LENGTH = 64;
    }

    public class ForLab {
        public static final int MAX_LAB_NAME_LENGTH = 32;

    }
    public class ForMedicalTest {
        public static final int MAX_MED_TEST_NAME_LENGTH = 64;

    }

    public class ForPatient {
        public static final int MAX_PATIENT_NAME_LENGTH = 64;
        public static final int MAX_PIN_CODE_LENGTH = 6;
    }

    public class ForPharmacy {
        public static final int MAX_PHARMACY_NAME_LENGTH = 64;

    }

    public class ForUser {
        public static final int MAX_PHONE_NUMBER_LENGTH = 15;
        public static final int MAX_PASSWORD_LENGTH = 128;
        public static final int MAX_EMAIL_LENGTH = 64;
        public static final int MAX_FIRST_NAME_LENGTH = 32;
        public static final int MAX_LAST_NAME_LENGTH = 32;
    }

    public class ForUserShop {
        public static final int MAX_PHONE_NUMBER_LENGTH = 15;
        public static final int MAX_EMAIL_LENGTH = 64;
    }

    public static final String ROLE_PATIENT="PATIENT";
    public static final String ROLE_DOCTOR="DOCTOR";
    public static final String ROLE_USER ="USER";
    public static final String ROLE_SALES_ADMIN="SALES";
    public static final String ROLE_MARKETING_ADMIN="MARKETING";
    public static final String ROLE_ADMIN="ADMIN";

    public static String getCurrentDate(){
        Date date = new Date();
        SimpleDateFormat formatter  = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        String strDate = formatter.format(date);
        return strDate;
    }

    public static String getCurrentTime(){
        Date time = new Date(System.currentTimeMillis());
       String currentTime=new SimpleDateFormat("HH:mm:ss").format(time);
       return currentTime;
    }
}
