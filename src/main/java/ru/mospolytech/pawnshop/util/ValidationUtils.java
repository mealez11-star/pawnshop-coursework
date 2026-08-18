package ru.mospolytech.pawnshop.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class ValidationUtils {
    private ValidationUtils() {
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Поле «" + fieldName + "» не заполнено");
        }
        return value.trim();
    }

    public static int parsePositiveInt(String value, String fieldName) {
        try {
            int result = Integer.parseInt(requireText(value, fieldName));
            if (result <= 0) {
                throw new NumberFormatException();
            }
            return result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + fieldName + "» должно быть положительным целым числом");
        }
    }

    public static BigDecimal parsePositiveMoney(String value, String fieldName, boolean zeroAllowed) {
        try {
            BigDecimal result = new BigDecimal(requireText(value, fieldName).replace(',', '.'));
            int comparison = result.compareTo(BigDecimal.ZERO);
            if ((zeroAllowed && comparison < 0) || (!zeroAllowed && comparison <= 0)) {
                throw new NumberFormatException();
            }
            return result;
        } catch (NumberFormatException e) {
            String rule = zeroAllowed ? "неотрицательным" : "положительным";
            throw new IllegalArgumentException("Поле «" + fieldName + "» должно быть " + rule + " числом");
        }
    }

    public static LocalDate parseDate(String value, String fieldName) {
        try {
            return LocalDate.parse(requireText(value, fieldName));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Поле «" + fieldName + "» должно иметь формат ГГГГ-ММ-ДД");
        }
    }
}
