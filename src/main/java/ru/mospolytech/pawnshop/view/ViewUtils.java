package ru.mospolytech.pawnshop.view;

import javax.swing.*;
import java.awt.*;

public final class ViewUtils {
    private ViewUtils() {
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Подтверждение",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
