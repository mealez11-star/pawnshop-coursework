package ru.mospolytech.pawnshop.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

public final class PasswordUtils {
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;
    private static final Pattern STRONG_PASSWORD = Pattern.compile(
            "^(?=.*\\p{Lu})(?=.*\\d)(?=.*[^\\p{L}\\p{N}\\s]).{8,}$"
    );

    private PasswordUtils() {
    }

    public static boolean isStrong(String password) {
        return password != null && STRONG_PASSWORD.matcher(password).matches();
    }

    public static String hash(String password) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            byte[] result = pbkdf2(password.toCharArray(), salt, ITERATIONS);
            return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt)
                    + ":" + Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось захешировать пароль", e);
        }
    }

    public static boolean matches(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 3) {
                return false;
            }
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            byte[] actual = pbkdf2(password.toCharArray(), salt, iterations);

            if (actual.length != expected.length) {
                return false;
            }
            int difference = 0;
            for (int i = 0; i < actual.length; i++) {
                difference |= actual[i] ^ expected[i];
            }
            return difference == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) throws Exception {
        KeySpec specification = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(specification)
                .getEncoded();
    }
}
