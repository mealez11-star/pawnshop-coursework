package ru.mospolytech.pawnshop.view;

import ru.mospolytech.pawnshop.model.Client;
import ru.mospolytech.pawnshop.model.ContractSummary;
import ru.mospolytech.pawnshop.model.Item;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ContractCardDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final String NEW_ITEM_MODE = "Новый товар";
    private static final String REPEAT_PLEDGE_MODE = "Повторный залог";

    private final JComboBox<Client> clientCombo = new JComboBox<>();
    private final JTextField issueDateField = new JTextField(18);
    private final JTextField dueDateField = new JTextField(18);
    private final JTextField loanField = new JTextField(18);
    private final JTextField commissionField = new JTextField(18);
    private final JLabel repaymentLabel = new JLabel();
    private final JButton saveDataButton = new JButton("Сохранить данные");
    private final JTabbedPane tabs = new JTabbedPane();

    private final DefaultTableModel itemsTableModel;
    private final JTable itemsTable;
    private final JComboBox<String> itemModeCombo = new JComboBox<>(
            new String[]{NEW_ITEM_MODE, REPEAT_PLEDGE_MODE});
    private final JTextField newItemNameField = new JTextField(24);
    private final JComboBox<Item> itemCombo = new JComboBox<>();
    private final JPanel itemSelectorPanel = new JPanel(new CardLayout());
    private final JTextField assessedValueField = new JTextField(12);
    private final JButton addItemButton = new JButton("Добавить в договор");
    private final JButton updateItemButton = new JButton("Изменить оценку");
    private final JButton removeItemButton = new JButton("Убрать товар");
    private final JLabel itemCountLabel = new JLabel("Количество товаров: 0");
    private final JLabel totalAssessedLabel = new JLabel("Общая оценка: 0,00 ₽");

    public ContractCardDialog(JFrame owner, List<Client> clients,
                              ContractSummary contract, boolean editable) {
        super(owner, "Карточка договора № " + contract.getContractId(), true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(880, 620);
        setMinimumSize(new Dimension(780, 540));
        setLocationRelativeTo(owner);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(12, 12, 10, 12));
        setContentPane(content);

        JLabel header = new JLabel("Карточка договора № " + contract.getContractId());
        header.setFont(header.getFont().deriveFont(Font.BOLD, 15f));
        content.add(header, BorderLayout.NORTH);

        for (Client client : clients) clientCombo.addItem(client);
        selectClient(contract.getClientId());
        issueDateField.setText(contract.getIssueDate().toString());
        dueDateField.setText(contract.getReturnDueDate().toString());
        loanField.setText(contract.getLoanAmount().toPlainString());
        commissionField.setText(contract.getCommissionAmount().toPlainString());

        itemsTableModel = new DefaultTableModel(
                new String[]{"ID товара", "Название товара", "Оценочная стоимость"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                if (columnIndex == 2) return BigDecimal.class;
                return String.class;
            }
        };
        itemsTable = new JTable(itemsTableModel);
        configureItemsTable();

        tabs.addTab("Данные договора", createDataTab(editable));
        tabs.addTab("Состав договора", createItemsTab(editable));
        content.add(tabs, BorderLayout.CENTER);

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(event -> dispose());
        closePanel.add(closeButton);
        content.add(closePanel, BorderLayout.SOUTH);

        installRepaymentUpdater();
        updateRepayment();
    }

    private JPanel createDataTab(boolean editable) {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel modeLabel = new JLabel(editable
                ? "Реквизиты договора можно изменить"
                : "Договор доступен только для просмотра");
        modeLabel.setFont(modeLabel.getFont().deriveFont(Font.BOLD));
        tab.add(modeLabel, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Основные данные"));
        addFormRow(form, 0, "Клиент:", clientCombo);
        addFormRow(form, 1, "Дата выдачи:", issueDateField);
        addFormRow(form, 2, "Срок возврата:", dueDateField);
        addFormRow(form, 3, "Сумма выдачи:", loanField);
        addFormRow(form, 4, "Комиссия:", commissionField);
        addFormRow(form, 5, "Сумма к возврату:", repaymentLabel);
        tab.add(form, BorderLayout.CENTER);

        clientCombo.setEnabled(editable);
        issueDateField.setEditable(editable);
        dueDateField.setEditable(editable);
        loanField.setEditable(editable);
        commissionField.setEditable(editable);
        issueDateField.setToolTipText("Формат: ГГГГ-ММ-ДД");
        dueDateField.setToolTipText("Формат: ГГГГ-ММ-ДД");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveDataButton.setVisible(editable);
        buttons.add(saveDataButton);
        tab.add(buttons, BorderLayout.SOUTH);
        return tab;
    }

    private JPanel createItemsTab(boolean editable) {
        JPanel tab = new JPanel(new BorderLayout(8, 8));
        tab.setBorder(new EmptyBorder(10, 10, 10, 10));

        if (editable) {
            JLabel hint = new JLabel("В договоре должен быть хотя бы один товар");
            hint.setFont(hint.getFont().deriveFont(Font.BOLD));
            tab.add(hint, BorderLayout.NORTH);
        }

        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Товары, входящие в договор"));
        tab.add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        JPanel totals = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        itemCountLabel.setFont(itemCountLabel.getFont().deriveFont(Font.BOLD));
        totalAssessedLabel.setFont(totalAssessedLabel.getFont().deriveFont(Font.BOLD));
        totals.add(itemCountLabel);
        totals.add(totalAssessedLabel);
        bottom.add(totals, BorderLayout.NORTH);

        if (editable) {
            JPanel input = new JPanel(new GridBagLayout());
            input.setBorder(BorderFactory.createTitledBorder("Добавление товара"));

            itemSelectorPanel.add(newItemNameField, NEW_ITEM_MODE);
            itemSelectorPanel.add(itemCombo, REPEAT_PLEDGE_MODE);
            itemModeCombo.addActionListener(event -> showSelectedItemInput());

            newItemNameField.setToolTipText("Например: Ноутбук Lenovo IdeaPad");
            itemCombo.setPreferredSize(new Dimension(300, itemCombo.getPreferredSize().height));
            assessedValueField.setToolTipText("Положительное число, например 30000");
            addFormRow(input, 0, "Способ:", itemModeCombo);
            addFormRow(input, 1, "Товар:", itemSelectorPanel);
            addFormRow(input, 2, "Оценочная стоимость:", assessedValueField);
            bottom.add(input, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            updateItemButton.setEnabled(false);
            removeItemButton.setEnabled(false);
            buttons.add(addItemButton);
            buttons.add(updateItemButton);
            buttons.add(removeItemButton);
            bottom.add(buttons, BorderLayout.SOUTH);

            showSelectedItemInput();
        }

        tab.add(bottom, BorderLayout.SOUTH);
        return tab;
    }

    private void configureItemsTable() {
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemsTable.setAutoCreateRowSorter(true);
        itemsTable.setFillsViewportHeight(true);
        itemsTable.setRowHeight(24);
        itemsTable.getTableHeader().setReorderingAllowed(false);
        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        itemsTable.getColumnModel().getColumn(1).setPreferredWidth(450);
        itemsTable.getColumnModel().getColumn(2).setPreferredWidth(190);

        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                super.setValue(value instanceof BigDecimal
                        ? formatMoney((BigDecimal) value) : value);
            }
        };
        itemsTable.getColumnModel().getColumn(2).setCellRenderer(moneyRenderer);
    }

    private void addFormRow(JPanel panel, int row, String caption, JComponent field) {
        GridBagConstraints label = new GridBagConstraints();
        label.gridx = 0;
        label.gridy = row;
        label.anchor = GridBagConstraints.WEST;
        label.insets = new Insets(6, 8, 6, 14);
        panel.add(new JLabel(caption), label);

        GridBagConstraints value = new GridBagConstraints();
        value.gridx = 1;
        value.gridy = row;
        value.weightx = 1;
        value.fill = GridBagConstraints.HORIZONTAL;
        value.insets = new Insets(6, 0, 6, 8);
        panel.add(field, value);
    }

    private void installRepaymentUpdater() {
        DocumentListener listener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { updateRepayment(); }
            @Override public void removeUpdate(DocumentEvent event) { updateRepayment(); }
            @Override public void changedUpdate(DocumentEvent event) { updateRepayment(); }
        };
        loanField.getDocument().addDocumentListener(listener);
        commissionField.getDocument().addDocumentListener(listener);
    }

    private void updateRepayment() {
        try {
            BigDecimal loan = new BigDecimal(loanField.getText().trim().replace(',', '.'));
            BigDecimal commission = new BigDecimal(commissionField.getText().trim().replace(',', '.'));
            repaymentLabel.setText(formatMoney(loan.add(commission)));
        } catch (NumberFormatException e) {
            repaymentLabel.setText("—");
        }
    }

    private void selectClient(int clientId) {
        for (int i = 0; i < clientCombo.getItemCount(); i++) {
            if (clientCombo.getItemAt(i).getId() == clientId) {
                clientCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    public void setItemsSummary(int itemCount, BigDecimal totalAssessed) {
        itemCountLabel.setText("Количество товаров: " + itemCount);
        totalAssessedLabel.setText("Общая оценка: " + formatMoney(totalAssessed));
    }

    public void setItemActionsEnabled(boolean enabled) {
        updateItemButton.setEnabled(enabled);
        removeItemButton.setEnabled(enabled);
    }

    public void selectItemsTab() {
        tabs.setSelectedIndex(1);
    }

    private void showSelectedItemInput() {
        String mode = (String) itemModeCombo.getSelectedItem();
        CardLayout layout = (CardLayout) itemSelectorPanel.getLayout();
        layout.show(itemSelectorPanel, mode == null ? NEW_ITEM_MODE : mode);
    }

    public boolean isNewItemMode() {
        return NEW_ITEM_MODE.equals(itemModeCombo.getSelectedItem());
    }

    public String getNewItemName() {
        return newItemNameField.getText();
    }

    public void clearItemInput() {
        newItemNameField.setText("");
        assessedValueField.setText("");
    }

    private static String formatMoney(BigDecimal value) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.forLanguageTag("ru-RU"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(value == null ? BigDecimal.ZERO : value) + " ₽";
    }

    public Client getSelectedClient() { return (Client) clientCombo.getSelectedItem(); }
    public JTextField getIssueDateField() { return issueDateField; }
    public JTextField getDueDateField() { return dueDateField; }
    public JTextField getLoanField() { return loanField; }
    public JTextField getCommissionField() { return commissionField; }
    public JButton getSaveDataButton() { return saveDataButton; }
    public DefaultTableModel getItemsTableModel() { return itemsTableModel; }
    public JTable getItemsTable() { return itemsTable; }
    public JComboBox<Item> getItemCombo() { return itemCombo; }
    public JTextField getAssessedValueField() { return assessedValueField; }
    public JButton getAddItemButton() { return addItemButton; }
    public JButton getUpdateItemButton() { return updateItemButton; }
    public JButton getRemoveItemButton() { return removeItemButton; }
}
