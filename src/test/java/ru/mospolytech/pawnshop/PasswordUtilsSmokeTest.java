package ru.mospolytech.pawnshop;

import ru.mospolytech.pawnshop.util.PasswordUtils;

/** Простой тест без сторонних библиотек. Запускается с ключом -ea. */
public class PasswordUtilsSmokeTest {
    public static void main(String[] args) {
        String password = "Test@123";
        String hash = PasswordUtils.hash(password);

        assert !hash.contains(password) : "В хеше не должно быть исходного пароля";
        assert PasswordUtils.matches(password, hash) : "Верный пароль должен подходить";
        assert !PasswordUtils.matches("Wrong@123", hash) : "Неверный пароль не должен подходить";
        assert PasswordUtils.isStrong(password) : "Сильный пароль должен пройти проверку";
        assert !PasswordUtils.isStrong("password") : "Слабый пароль не должен пройти проверку";

        System.out.println("PasswordUtilsSmokeTest: OK");
    }
}
