package ru.mospolytech.pawnshop.view;

import ru.mospolytech.pawnshop.model.Role;
import ru.mospolytech.pawnshop.model.User;

import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {
    private static final long serialVersionUID = 1L;
    private final ProfilePanel profilePanel = new ProfilePanel();
    private final UsersPanel usersPanel = new UsersPanel();
    private final ClientsPanel clientsPanel = new ClientsPanel();
    private final ContractsPanel contractsPanel = new ContractsPanel();
    private final ItemsPanel itemsPanel = new ItemsPanel();
    private final PricesPanel pricesPanel = new PricesPanel();
    private final SalesPanel salesPanel = new SalesPanel();
    private final ReportPanel reportPanel = new ReportPanel();
    private final JButton logoutButton = new JButton("Выйти");

    public MainView(User user) {
        super("Ломбард");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1120, 760);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel userLabel = new JLabel("Пользователь: " + user.getFullName() + " | Роль: " + user.getRole());
        userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD));
        header.add(userLabel, BorderLayout.WEST);
        header.add(logoutButton, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Профиль", profilePanel);
        if (user.getRole() == Role.ADMIN) {
            tabs.addTab("Пользователи", usersPanel);
            tabs.addTab("Клиенты", clientsPanel);
            tabs.addTab("Договоры", contractsPanel);
            tabs.addTab("Товары", itemsPanel);
            tabs.addTab("Цены", pricesPanel);
            tabs.addTab("Продажи", salesPanel);
            tabs.addTab("Финансовый отчёт", reportPanel);
        } else {
            contractsPanel.setReadOnlyMode();
            tabs.addTab("Мои договоры", contractsPanel);
        }
        add(tabs, BorderLayout.CENTER);
    }

    public ProfilePanel getProfilePanel() { return profilePanel; }
    public UsersPanel getUsersPanel() { return usersPanel; }
    public ClientsPanel getClientsPanel() { return clientsPanel; }
    public ContractsPanel getContractsPanel() { return contractsPanel; }
    public ItemsPanel getItemsPanel() { return itemsPanel; }
    public PricesPanel getPricesPanel() { return pricesPanel; }
    public SalesPanel getSalesPanel() { return salesPanel; }
    public ReportPanel getReportPanel() { return reportPanel; }
    public JButton getLogoutButton() { return logoutButton; }
}
