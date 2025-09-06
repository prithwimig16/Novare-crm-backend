package com.medwiz.novare_crm.utils;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class MRDGeneratorUtil {

    private static final String PREFIX = "NOVARE";

    public static String generateMRD() {
        int year = LocalDate.now().getYear();
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return String.format("%s-%d-%04d", PREFIX, year, random);
    }

    public static String generateUniqueMRD(Predicate<String> existsFn) {
        String mrd;
        int attempts = 0;

        do {
            mrd = generateMRD();
            attempts++;
            if (attempts > 10) {
                throw new RuntimeException("Unable to generate unique MRD number after 10 attempts");
            }
        } while (existsFn.test(mrd));

        return mrd;
    }
}
