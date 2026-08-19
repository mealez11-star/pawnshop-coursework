package ru.mospolytech.pawnshop.view;

import ru.mospolytech.pawnshop.model.Client;
import ru.mospolytech.pawnshop.model.ContractSummary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ContractEditDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final JComboBox<Client> clientCombo = new JComboBox<>();
    private final JTextField issueDateField = new JTextField(18);
    private final JTextField dueDateField = new JTextField(18);
    private final JTextField loanField = new JTextField(18);
    private final JTextField commissionField = new JTextField(18);
    private final JButton saveButton = new JButton("Сохранить");

    public ContractEditDialog(JFrame owner, List<Client> clients, ContractSummary contract) {
        super(owner, contract == null
                ? "Новый договор"
                : "Изменение договора № " + contract.getContractId(), true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(14, 14, 12, 14));
        setContentPane(content);

        JLabel hint = new JLabel(contract == null
                ? "Заполните основные данные нового договора"
                : "Измените нужные поля и нажмите «Сохранить»");
        hint.setFont(hint.getFont().deriveFont(Font.BOLD));
        content.add(hint, BorderLayout.NORTH);

        for (Client client : clients) {
            clientCombo.addItem(client);
        }
        issueDateField.setToolTipText("Формат: ГГГГ-ММ-ДД");
        dueDateField.setToolTipText("Формат: ГГГГ-ММ-ДД");
        loanField.setToolTipText("Положительное число, например 20000");
        commissionField.setToolTipText("Число не меньше нуля, например 2000");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Данные договора"));
        addFormRow(form, 0, "Клиент:", clientCombo);
        addFormRow(form, 1, "Дата выдачи:", issueDateField);
        addFormRow(form, 2, "Срок возврата:", dueDateField);
        addFormRow(form, 3, "Сумма выдачи:", loanField);
        addFormRow(form, 4, "Комиссия:", commissionField);
        content.add(form, BorderLayout.CENTER);

        if (contract == null) {
            LocalDate today = LocalDate.now();
            issueDateField.setText(today.toString());
            dueDateField.setText(today.plusDays(30).toString());
        } else {
            selectClient(contract.getClientId());
            issueDateField.setText(contract.getIssueDate().toString());
            dueDateField.setText(contract.getReturnDueDate().toString());
            loanField.setText(contract.getLoanAmount().toPlainString());
            commissionField.setText(contract.getCommissionAmount().toPlainString());
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(event -> dispose());
        buttons.add(cancelButton);
        buttons.add(saveButton);
        content.add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveButton);
        pack();
        setSize(Math.max(getWidth(), 500), getHeight());
        setLocationRelativeTo(owner);
    }

    private void addFormRow(JPanel panel, int row, String caption, JComponent field) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(5, 6, 5, 12);
        panel.add(new JLabel(caption), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(5, 0, 5, 6);
        panel.add(field, fieldConstraints);
    }

    private void selectClient(int clientId) {
        for (int i = 0; i < clientCombo.getItemCount(); i++) {
            if (clientCombo.getItemAt(i).getId() == clientId) {
                clientCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    public Client getSelectedClient() { return (Client) clientCombo.getSelectedItem(); }
    public JTextField getIssueDateField() { return issueDateField; }
    public JTextField getDueDateField() { return dueDateField; }
    public JTextField getLoanField() { return loanField; }
    public JTextField getCommissionField() { return commissionField; }
    public JButton getSaveButton() { return saveButton; }
}
