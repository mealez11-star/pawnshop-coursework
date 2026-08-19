package ru.mospolytech.pawnshop.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class ContractsPanel extends BaseTablePanel {
    private static final long serialVersionUID = 1L;

    public ContractsPanel() {
        super(new String[]{"ID", "Клиент", "Дата выдачи", "Срок возврата",
                "Сумма выдачи", "Комиссия", "Общая оценка", "Товаров"});
        setFormVisible(false);
        getAddButton().setText("Новый договор");
        getUpdateButton().setText("Открыть договор");

        getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        getTable().setFillsViewportHeight(true);
        getTable().setRowHeight(24);
        getTable().getTableHeader().setReorderingAllowed(false);
        int[] widths = {45, 220, 100, 100, 115, 100, 115, 65};
        for (int i = 0; i < widths.length; i++) {
            getTable().getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        installTableRenderers();
    }

    private void installTableRenderers() {
        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                            boolean selected, boolean focused,
                                                            int row, int column) {
                Object displayedValue = value instanceof BigDecimal
                        ? formatMoney((BigDecimal) value)
                        : value;
                setHorizontalAlignment(SwingConstants.RIGHT);
                return super.getTableCellRendererComponent(
                        table, displayedValue, selected, focused, row, column);
            }
        };
        for (int column = 4; column <= 6; column++) {
            getTable().getColumnModel().getColumn(column).setCellRenderer(moneyRenderer);
        }
    }

    private String formatMoney(BigDecimal value) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.forLanguageTag("ru-RU"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(value) + " ₽";
    }

    public void setReadOnlyMode() {
        getAddButton().setVisible(false);
        getDeleteButton().setVisible(false);
    }
}
