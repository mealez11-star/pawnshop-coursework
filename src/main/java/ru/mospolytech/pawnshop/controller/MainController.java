package ru.mospolytech.pawnshop.controller;

import ru.mospolytech.pawnshop.config.DatabaseConnection;
import ru.mospolytech.pawnshop.dao.*;
import ru.mospolytech.pawnshop.model.*;
import ru.mospolytech.pawnshop.util.PasswordUtils;
import ru.mospolytech.pawnshop.util.ValidationUtils;
import ru.mospolytech.pawnshop.view.*;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainController {
    private final User currentUser;
    private final MainView view;
    private final Runnable logoutAction;

    private final UserDao userDao = new UserDao();
    private final ClientDao clientDao = new ClientDao();
    private final ContractDao contractDao = new ContractDao();
    private final ItemDao itemDao = new ItemDao();
    private final PriceDao priceDao = new PriceDao();
    private final SaleDao saleDao = new SaleDao();
    private final ReportDao reportDao = new ReportDao();

    private List<User> users = new ArrayList<>();
    private List<Client> clients = new ArrayList<>();
    private List<ContractSummary> contracts = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private List<Price> prices = new ArrayList<>();
    private List<Sale> sales = new ArrayList<>();

    public MainController(User currentUser, MainView view, Runnable logoutAction) {
        this.currentUser = currentUser;
        this.view = view;
        this.logoutAction = logoutAction;
        bindCommonActions();
        if (isAdmin()) {
            bindAdminActions();
        }
        loadInitialData();
    }

    private void bindCommonActions() {
        view.getLogoutButton().addActionListener(event -> logout());
        view.getProfilePanel().getSaveButton().addActionListener(event -> updateProfile());
        view.getContractsPanel().getRefreshButton().addActionListener(event -> refreshContracts());
        view.getContractsPanel().getUpdateButton().addActionListener(event -> openContractCard());
        view.getContractsPanel().getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(event)) {
                    int row = view.getContractsPanel().getTable().rowAtPoint(event.getPoint());
                    if (row >= 0) {
                        view.getContractsPanel().getTable().setRowSelectionInterval(row, row);
                        openContractCard();
                    }
                }
            }
        });
    }

    private void bindAdminActions() {
        UsersPanel usersPanel = view.getUsersPanel();
        usersPanel.getRefreshButton().addActionListener(event -> refreshUsers());
        usersPanel.getUpdateButton().addActionListener(event -> updateUserRole());
        usersPanel.getDeleteButton().addActionListener(event -> deleteUser());
        usersPanel.getTable().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) fillUserForm();
        });

        ClientsPanel clientsPanel = view.getClientsPanel();
        clientsPanel.getRefreshButton().addActionListener(event -> refreshClients());
        clientsPanel.getUpdateButton().addActionListener(event -> updateClient());
        clientsPanel.getDeleteButton().addActionListener(event -> deleteClient());
        clientsPanel.getTable().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) fillClientForm();
        });

        ContractsPanel contractsPanel = view.getContractsPanel();
        contractsPanel.getAddButton().addActionListener(event -> createContract());
        contractsPanel.getDeleteButton().addActionListener(event -> deleteContract());

        ItemsPanel itemsPanel = view.getItemsPanel();
        itemsPanel.getRefreshButton().addActionListener(event -> refreshItems());
        itemsPanel.getAddButton().addActionListener(event -> createItem());
        itemsPanel.getUpdateButton().addActionListener(event -> updateItem());
        itemsPanel.getDeleteButton().addActionListener(event -> deleteItem());
        itemsPanel.getTable().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) fillItemForm();
        });

        PricesPanel pricesPanel = view.getPricesPanel();
        pricesPanel.getRefreshButton().addActionListener(event -> refreshPrices());
        pricesPanel.getAddButton().addActionListener(event -> createPrice());
        pricesPanel.getUpdateButton().addActionListener(event -> updatePrice());
        pricesPanel.getDeleteButton().addActionListener(event -> deletePrice());
        pricesPanel.getTable().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) fillPriceForm();
        });

        SalesPanel salesPanel = view.getSalesPanel();
        salesPanel.getRefreshButton().addActionListener(event -> refreshSales());
        salesPanel.getAddButton().addActionListener(event -> createSale());
        salesPanel.getUpdateButton().addActionListener(event -> updateSale());
        salesPanel.getDeleteButton().addActionListener(event -> deleteSale());
        salesPanel.getItemCombo().addActionListener(event -> refreshSalePriceCombo(null));
        salesPanel.getTable().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) fillSaleForm();
        });

        view.getReportPanel().getBuildButton().addActionListener(event -> buildReport());
    }

    private void loadInitialData() {
        loadProfile();
        if (isAdmin()) {
            refreshUsers();
            refreshClients();
            refreshItems();
            refreshPrices();
            refreshSales();
        }
        refreshContracts();
    }

    private void loadProfile() {
        runDatabaseAction(() -> {
            ProfilePanel panel = view.getProfilePanel();
            panel.getLoginField().setText(currentUser.getLogin());
            panel.getFullNameField().setText(currentUser.getFullName());
            Optional<Client> client = clientDao.findByUserId(currentUser.getId());
            if (client.isPresent()) {
                panel.setPassportEnabled(true);
                panel.getPassportField().setText(client.get().getPassportData());
            } else {
                panel.setPassportEnabled(false);
            }
        });
    }

    private void updateProfile() {
        try {
            ProfilePanel panel = view.getProfilePanel();
            String login = ValidationUtils.requireText(panel.getLoginField().getText(), "Логин");
            String fullName = ValidationUtils.requireText(panel.getFullNameField().getText(), "ФИО");
            Optional<Client> client = clientDao.findByUserId(currentUser.getId());
            String passport = client.isPresent()
                    ? ValidationUtils.requireText(panel.getPassportField().getText(), "Паспортные данные")
                    : "";
            String password = new String(panel.getPasswordField().getPassword());
            String passwordHash = null;
            if (!password.isBlank()) {
                if (!PasswordUtils.isStrong(password)) {
                    throw new IllegalArgumentException(
                            "Новый пароль должен содержать не менее 8 символов, заглавную букву, цифру и спецсимвол"
                    );
                }
                passwordHash = PasswordUtils.hash(password);
            }

            userDao.updateProfile(currentUser.getId(), login, fullName, passport, passwordHash);
            currentUser.setLogin(login);
            currentUser.setFullName(fullName);
            if (passwordHash != null) currentUser.setPasswordHash(passwordHash);
            panel.getPasswordField().setText("");
            ViewUtils.showInfo(view, "Данные профиля изменены");
            if (isAdmin()) {
                refreshUsers();
                refreshClients();
            }
        } catch (IllegalArgumentException e) {
            ViewUtils.showError(view, e.getMessage());
        } catch (SQLException e) {
            showSqlError(e);
        }
    }

    private void refreshUsers() {
        if (!isAdmin()) return;
        runDatabaseAction(() -> {
            users = userDao.findAll();
            Object[][] rows = new Object[users.size()][];
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                rows[i] = new Object[]{user.getId(), user.getLogin(), user.getFullName(), user.getRole()};
            }
            view.getUsersPanel().setRows(rows);
        });
    }

    private void fillUserForm() {
        int row = view.getUsersPanel().getSelectedModelRow();
        if (row >= 0 && row < users.size()) {
            view.getUsersPanel().getRoleCombo().setSelectedItem(users.get(row).getRole());
        }
    }

    private void updateUserRole() {
        int row = requireSelectedRow(view.getUsersPanel(), "пользователя");
        if (row < 0) return;
        User user = users.get(row);
        Role role = (Role) view.getUsersPanel().getRoleCombo().getSelectedItem();
        if (user.getId() == currentUser.getId() && role != Role.ADMIN) {
            ViewUtils.showError(view, "Нельзя снять роль администратора у своей текущей учётной записи");
            return;
        }
        runDatabaseAction(() -> {
            userDao.updateRole(user.getId(), role);
            refreshUsers();
        });
    }

    private void deleteUser() {
        int row = requireSelectedRow(view.getUsersPanel(), "пользователя");
        if (row < 0) return;
        User user = users.get(row);
        if (user.getId() == currentUser.getId()) {
            ViewUtils.showError(view, "Нельзя удалить свою текущую учётную запись");
            return;
        }
        if (!ViewUtils.confirm(view, "Удалить пользователя «" + user.getLogin() + "» и его клиента?")) return;
        runDatabaseAction(() -> {
            userDao.delete(user.getId());
            refreshUsers();
            refreshClients();
        });
    }

    private void refreshClients() {
        if (!isAdmin()) return;
        runDatabaseAction(() -> {
            clients = clientDao.findAll();
            Object[][] rows = new Object[clients.size()][];
            for (int i = 0; i < clients.size(); i++) {
                Client client = clients.get(i);
                rows[i] = new Object[]{client.getId(), client.getFullName(),
                        client.getPassportData(), client.getUserId()};
            }
            view.getClientsPanel().setRows(rows);
        });
    }

    private void fillClientForm() {
        int row = view.getClientsPanel().getSelectedModelRow();
        if (row >= 0 && row < clients.size()) {
            Client client = clients.get(row);
            view.getClientsPanel().getFullNameField().setText(client.getFullName());
            view.getClientsPanel().getPassportField().setText(client.getPassportData());
        }
    }

    private void updateClient() {
        int row = requireSelectedRow(view.getClientsPanel(), "клиента");
        if (row < 0) return;
        try {
            Client old = clients.get(row);
            Client updated = new Client(old.getId(),
                    ValidationUtils.requireText(view.getClientsPanel().getFullNameField().getText(), "ФИО"),
                    ValidationUtils.requireText(view.getClientsPanel().getPassportField().getText(), "Паспорт"),
                    old.getUserId());
            clientDao.update(updated);
            refreshClients();
            refreshContracts();
        } catch (IllegalArgumentException e) {
            ViewUtils.showError(view, e.getMessage());
        } catch (SQLException e) {
            showSqlError(e);
        }
    }

    private void deleteClient() {
        int row = requireSelectedRow(view.getClientsPanel(), "клиента");
        if (row < 0) return;
        Client client = clients.get(row);
        if (!ViewUtils.confirm(view, "Удалить клиента «" + client.getFullName() + "»?")) return;
        runDatabaseAction(() -> {
            clientDao.delete(client.getId());
            refreshClients();
        });
    }

    private void refreshContracts() {
        runDatabaseAction(() -> {
            contracts = isAdmin()
                    ? contractDao.findAllSummaries()
                    : contractDao.findSummariesForUser(currentUser.getId());
            Object[][] rows = new Object[contracts.size()][];
            for (int i = 0; i < contracts.size(); i++) {
                ContractSummary contract = contracts.get(i);
                rows[i] = new Object[]{contract.getContractId(), contract.getClientName(),
                        contract.getIssueDate(), contract.getReturnDueDate(),
                        contract.getLoanAmount(), contract.getCommissionAmount(),
                        contract.getTotalAssessedValue(), contract.getItemCount()};
            }
            view.getContractsPanel().setRows(rows);
        });
    }

    private void createContract() {
        if (clients.isEmpty()) {
            ViewUtils.showError(view, "Сначала зарегистрируйте хотя бы одного клиента");
            return;
        }
        ContractEditDialog dialog = new ContractEditDialog(view, clients, null);
        dialog.getSaveButton().addActionListener(event -> {
            try {
                Contract contract = readContractForm(dialog, 0);
                Client client = dialog.getSelectedClient();
                int contractId = contractDao.create(contract);
                ContractSummary createdContract = new ContractSummary(
                        contractId,
                        client.getId(),
                        client.getFullName(),
                        contract.getIssueDate(),
                        contract.getReturnDueDate(),
                        contract.getCommissionAmount(),
                        contract.getLoanAmount(),
                        BigDecimal.ZERO,
                        0
                );
                dialog.dispose();
                refreshContracts();
                openContractCard(createdContract, true);
            } catch (IllegalArgumentException e) {
                ViewUtils.showError(dialog, e.getMessage());
            } catch (SQLException e) {
                ViewUtils.showError(dialog, sqlMessage(e));
            }
        });
        dialog.setVisible(true);
    }

    private Contract readContractForm(ContractEditDialog dialog, int id) {
        return buildContract(id, dialog.getSelectedClient(),
                dialog.getIssueDateField().getText(), dialog.getDueDateField().getText(),
                dialog.getCommissionField().getText(), dialog.getLoanField().getText());
    }

    private Contract readContractForm(ContractCardDialog dialog, int id) {
        return buildContract(id, dialog.getSelectedClient(),
                dialog.getIssueDateField().getText(), dialog.getDueDateField().getText(),
                dialog.getCommissionField().getText(), dialog.getLoanField().getText());
    }

    private Contract buildContract(int id, Client client, String issueDateText,
                                   String dueDateText, String commissionText,
                                   String loanText) {
        if (client == null) throw new IllegalArgumentException("Выберите клиента");
        LocalDate issueDate = ValidationUtils.parseDate(issueDateText, "Дата выдачи");
        LocalDate dueDate = ValidationUtils.parseDate(dueDateText, "Срок возврата");
        if (issueDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата выдачи не может быть в будущем");
        }
        if (dueDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("Срок возврата не может быть раньше даты выдачи");
        }
        return new Contract(id, issueDate, dueDate,
                ValidationUtils.parsePositiveMoney(commissionText, "Комиссия", true),
                ValidationUtils.parsePositiveMoney(loanText, "Сумма выдачи", false),
                client.getId());
    }

    private void deleteContract() {
        int row = requireSelectedRow(view.getContractsPanel(), "договор");
        if (row < 0) return;
        int contractId = contracts.get(row).getContractId();
        if (!ViewUtils.confirm(view, "Удалить договор № " + contractId + "?")) return;
        runDatabaseAction(() -> {
            contractDao.delete(contractId);
            refreshContracts();
        });
    }

    private void openContractCard() {
        int row = requireSelectedRow(view.getContractsPanel(), "договор");
        if (row < 0) return;
        openContractCard(contracts.get(row), false);
    }

    private void openContractCard(ContractSummary contract, boolean selectItemsTab) {
        int contractId = contract.getContractId();
        try {
            boolean editable = isAdmin() && contractDao.isEditable(contractId);
            List<Client> cardClients = isAdmin()
                    ? clients
                    : clientDao.findByUserId(currentUser.getId())
                    .map(List::of).orElseGet(List::of);
            ContractCardDialog dialog = new ContractCardDialog(
                    view, cardClients, contract, editable);
            if (selectItemsTab) {
                dialog.selectItemsTab();
            }
            List<ContractItem> contractItems = new ArrayList<>();

            dialog.getSaveDataButton().addActionListener(event -> {
                try {
                    contractDao.update(readContractForm(dialog, contractId));
                    refreshContracts();
                    ViewUtils.showInfo(dialog, "Данные договора сохранены");
                } catch (IllegalArgumentException e) {
                    ViewUtils.showError(dialog, e.getMessage());
                } catch (SQLException e) {
                    ViewUtils.showError(dialog, sqlMessage(e));
                }
            });

            Runnable refresh = () -> runDatabaseAction(() -> {
                contractItems.clear();
                contractItems.addAll(contractDao.findItems(contractId));
                dialog.getItemsTableModel().setRowCount(0);
                BigDecimal totalAssessed = BigDecimal.ZERO;
                for (ContractItem item : contractItems) {
                    dialog.getItemsTableModel().addRow(new Object[]{
                            item.getItemId(), item.getItemName(), item.getAssessedValue()
                    });
                    totalAssessed = totalAssessed.add(item.getAssessedValue());
                }
                dialog.setItemsSummary(contractItems.size(), totalAssessed);
                dialog.setItemActionsEnabled(false);
                if (editable) {
                    refillCombo(dialog.getItemCombo(), contractDao.findItemsAvailableForPledge());
                }
            });
            refresh.run();

            dialog.getItemsTable().getSelectionModel().addListSelectionListener(event -> {
                if (event.getValueIsAdjusting()) return;
                int selected = dialog.getItemsTable().getSelectedRow();
                dialog.setItemActionsEnabled(selected >= 0 && editable);
                if (selected < 0 || !editable) return;
                int modelRow = dialog.getItemsTable().convertRowIndexToModel(selected);
                dialog.getAssessedValueField().setText(
                        contractItems.get(modelRow).getAssessedValue().toPlainString()
                );
            });

            dialog.getAddItemButton().addActionListener(event -> {
                try {
                    BigDecimal assessed = ValidationUtils.parsePositiveMoney(
                            dialog.getAssessedValueField().getText(), "Оценочная стоимость", false);
                    if (dialog.isNewItemMode()) {
                        String itemName = ValidationUtils.requireText(
                                dialog.getNewItemName(), "Название товара");
                        contractDao.createAndAddNewItem(contractId, itemName, assessed);
                    } else {
                        Item item = (Item) dialog.getItemCombo().getSelectedItem();
                        if (item == null) {
                            throw new IllegalArgumentException(
                                    "Нет возвращённого товара для повторного залога");
                        }
                        contractDao.addItem(contractId, item.getId(), assessed);
                    }
                    dialog.clearItemInput();
                    refresh.run();
                } catch (IllegalArgumentException e) {
                    ViewUtils.showError(dialog, e.getMessage());
                } catch (SQLException e) {
                    ViewUtils.showError(dialog, sqlMessage(e));
                }
            });

            dialog.getUpdateItemButton().addActionListener(event -> {
                int selected = dialog.getItemsTable().getSelectedRow();
                if (selected < 0) {
                    ViewUtils.showError(dialog, "Выберите товар в таблице");
                    return;
                }
                int modelRow = dialog.getItemsTable().convertRowIndexToModel(selected);
                ContractItem item = contractItems.get(modelRow);
                try {
                    BigDecimal assessed = ValidationUtils.parsePositiveMoney(
                            dialog.getAssessedValueField().getText(), "Оценочная стоимость", false);
                    contractDao.updateAssessedValue(contractId, item.getItemId(), assessed);
                    refresh.run();
                } catch (IllegalArgumentException e) {
                    ViewUtils.showError(dialog, e.getMessage());
                } catch (SQLException e) {
                    ViewUtils.showError(dialog, sqlMessage(e));
                }
            });

            dialog.getRemoveItemButton().addActionListener(event -> {
                int selected = dialog.getItemsTable().getSelectedRow();
                if (selected < 0) {
                    ViewUtils.showError(dialog, "Выберите товар в таблице");
                    return;
                }
                int modelRow = dialog.getItemsTable().convertRowIndexToModel(selected);
                ContractItem item = contractItems.get(modelRow);
                if (!ViewUtils.confirm(dialog,
                        "Убрать товар «" + item.getItemName() + "» из договора?")) {
                    return;
                }
                try {
                    contractDao.removeItem(contractId, item.getItemId());
                    dialog.getAssessedValueField().setText("");
                    refresh.run();
                } catch (SQLException e) {
                    ViewUtils.showError(dialog, sqlMessage(e));
                }
            });

            dialog.setVisible(true);
            if (editable && contractDao.findItems(contractId).isEmpty()) {
                contractDao.delete(contractId);
                ViewUtils.showInfo(view,
                        "Пустой договор № " + contractId + " удалён. "
                                + "В договоре должен быть хотя бы один товар");
            }
            refreshContracts();
            if (isAdmin()) refreshItems();
        } catch (SQLException e) {
            showSqlError(e);
        }
    }

    private void refreshItems() {
        if (!isAdmin()) return;
        runDatabaseAction(() -> {
            items = itemDao.findAll();
            Object[][] rows = new Object[items.size()][];
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                rows[i] = new Object[]{item.getId(), item.getName(), item.getStatus()};
            }
            view.getItemsPanel().setRows(rows);
            refillOwnedItems(view.getPricesPanel().getItemCombo());
            refillOwnedItems(view.getSalesPanel().getItemCombo());
        });
    }

    /** Новую цену или продажу можно оформить только на собственность ломбарда. */
    private void refillOwnedItems(JComboBox<Item> combo) {
        combo.removeAllItems();
        for (Item item : items) {
            if (item.getStatus() == ItemStatus.OWNED_BY_PAWNSHOP) {
                combo.addItem(item);
            }
        }
    }

    private void fillItemForm() {
        int row = view.getItemsPanel().getSelectedModelRow();
        if (row >= 0 && row < items.size()) {
            Item item = items.get(row);
            view.getItemsPanel().getNameField().setText(item.getName());
            view.getItemsPanel().getStatusCombo().setSelectedItem(item.getStatus());
        }
    }

    private void createItem() {
        try {
            ItemsPanel panel = view.getItemsPanel();
            ItemStatus status = (ItemStatus) panel.getStatusCombo().getSelectedItem();
            if (status != ItemStatus.PLEDGED) {
                throw new IllegalArgumentException("Новый товар сначала создаётся со статусом «В залоге»");
            }
            Item item = new Item(0,
                    ValidationUtils.requireText(panel.getNameField().getText(), "Название"),
                    status);
            itemDao.create(item);
            refreshItems();
        } catch (IllegalArgumentException e) {
            ViewUtils.showError(view, e.getMessage());
        } catch (SQLException e) {
            showSqlError(e);
        }
    }

    private void updateItem() {
        int row = requireSelectedRow(view.getItemsPanel(), "товар");
        if (row < 0) return;
        try {
            ItemsPanel panel = view.getItemsPanel();
            Item oldItem = items.get(row);
            ItemStatus newStatus = (ItemStatus) panel.getStatusCombo().getSelectedItem();
            validateStatusChange(oldItem, newStatus);
            Item item = new Item(items.get(row).getId(),
                    ValidationUtils.requireText(panel.getNameField().getText(), "Название"),
                    newStatus);
            itemDao.update(item);
            refreshItems();
            refreshPrices();
            refreshSales();
        } catch (IllegalArgumentException e) {
            ViewUtils.showError(view, e.getMessage());
        } catch (SQLException e) {
            showSqlError(e);
        }
    }

    private void deleteItem() {
        int row = requireSelectedRow(view.getItemsPanel(), "товар");
        if (row < 0) return;
        Item item = items.get(row);
        if (!ViewUtils.confirm(view, "Удалить товар «" + item.getName() + "»?")) return;
        runDatabaseAction(() -> {
            itemDao.delete(item.getId());
            refreshItems();
            refreshPrices();
        });
    }

    private void refreshPrices() {
        if (!isAdmin()) return;
        runDatabaseAction(() -> {
            prices = priceDao.findAll();
            Object[][] rows = new Object[prices.size()][];
            for (int i = 0; i < prices.size(); i++) {
                Price price = prices.get(i);
                rows[i] = new Object[]{price.getId(), price.getItemName(), price.getDate(), price.getValue()};
            }
            view.getPricesPanel().setRows(rows);
            refreshSalePriceCombo(null);
        });
    }

    private void fillPriceForm() {
        int row = view.getPricesPanel().getSelectedModelRow();
        if (row < 0 || row >= prices.size()) return;
        Price price = prices.get(row);
        ensureItemPresent(view.getPricesPanel().getItemCombo(), price.getItemId());
        selectItem(view.getPricesPanel().getItemCombo(), price.getItemId());
        view.getPricesPanel().getDateField().setText(price.getDate().toString());
        view.getPricesPanel().getValueField().setText(price.getValue().toPlainString());
    }

    private Price readPriceForm(int id) throws SQLException {
        PricesPanel panel = view.getPricesPanel();
        Item item = (Item) panel.getItemCombo().getSelectedItem();
        if (item == null) throw new IllegalArgumentException("Сначала добавьте товар");
        LocalDate priceDate = ValidationUtils.parseDate(panel.getDateField().getText(), "Дата цены");
        if (priceDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата цены не может быть в будущем");
        }
        requireOperationAfterDeadline(item.getId(), priceDate, "Дата цены");
        return new Price(id, priceDate,
                ValidationUtils.parsePositiveMoney(panel.getValueField().getText(), "Цена", false),
                item.getId(), item.getName());
    }

    private void createPrice() {
        try {
            Item item = (Item) view.getPricesPanel().getItemCombo().getSelectedItem();
            if (item == null || item.getStatus() != ItemStatus.OWNED_BY_PAWNSHOP) {
                throw new IllegalArgumentException(
                        "Добавить цену можно только товару со статусом «Собственность ломбарда»"
                );
            }
            priceDao.create(readPriceForm(0));
            refreshPrices();
        } catch (IllegalArgumentException e) {
            ViewUtils.showError(view, e.getMessage());
        } catch (SQLException e) {
            showSqlError(e);
        }
    }

    private void updatePrice() {
        int row = requireSelectedRow(view.getPricesPanel(), "цену");
        if (row < 0) return;
        try {
            priceDao.update(readPriceForm(prices.get(row).getId()));
            refreshPrices();
            refreshSales();
        } catch (IllegalArgumentException e) {
            ViewUtils.showError(view, e.getMessage());
        } catch (SQLException e) {
            showSqlError(e);
        }
    }

    private void deletePrice() {
        int row = requireSelectedRow(view.getPricesPanel(), "цену");
        if (row < 0) return;
        Price price = prices.get(row);
        if (!ViewUtils.confirm(view, "Удалить цену от " + price.getDate() + "?")) return;
        runDatabaseAction(() -> {
            priceDao.delete(price.getId());
            refreshPrices();
        });
    }

    private void refreshSales() {
        if (!isAdmin()) return;
        runDatabaseAction(() -> {
            sales = saleDao.findAll();
            Object[][] rows = new Object[sales.size()][];
            for (int i = 0; i < sales.size(); i++) {
                Sale sale = sales.get(i);
                rows[i] = new Object[]{sale.getId(), sale.getSaleDate(), sale.getItemName(),
                        sale.getPriceId(), sale.getSalePrice()};
            }
            view.getSalesPanel().setRows(rows);
        });
    }

    private void fillSaleForm() {
        int row = view.getSalesPanel().getSelectedModelRow();
        if (row < 0 || row >= sales.size()) return;
        Sale sale = sales.get(row);
        view.getSalesPanel().getDateField().setText(sale.getSaleDate().toString());
        ensureItemPresent(view.getSalesPanel().getItemCombo(), sale.getItemId());
        selectItem(view.getSalesPanel().getItemCombo(), sale.getItemId());
        refreshSalePriceCombo(sale.getPriceId());
    }

    private Sale readSaleForm(int id) throws SQLException {
        SalesPanel panel = view.getSalesPanel();
        Item item = (Item) panel.getItemCombo().getSelectedItem();
        Price price = (Price) panel.getPriceCombo().getSelectedItem();
        if (item == null) throw new IllegalArgumentException("Выберите товар");
        if (price == null) throw new IllegalArgumentException("У выбранного товара нет цены");
        LocalDate saleDate = ValidationUtils.parseDate(panel.getDateField().getText(), "Дата продажи");
        if (saleDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата продажи не может быть в будущем");
        }
        requireOperationAfterDeadline(item.getId(), saleDate, "Дата продажи");
        if (saleDate.isBefore(price.getDate())) {
            throw new IllegalArgumentException("Дата продажи не может быть раньше даты выбранной цены");
        }
        return new Sale(id, saleDate,
                item.getId(), item.getName(), price.getId(), price.getValue());
    }

    private void createSale() {
        try {
            Item item = (Item) view.getSalesPanel().getItemCombo().getSelectedItem();
            if (item == null || item.getStatus() != ItemStatus.OWNED_BY_PAWNSHOP) {
                throw new IllegalArgumentException(
                        "Продать можно только товар со статусом «Собственность ломбарда»"
                );
            }
            saleDao.create(readSaleForm(0));
            refreshItems();
            refreshSales();
        } catch (IllegalArgumentException e) {
            ViewUtils.showError(view, e.getMessage());
        } catch (SQLException e) {
            showSqlError(e);
        }
    }

    private void updateSale() {
        int row = requireSelectedRow(view.getSalesPanel(), "продажу");
        if (row < 0) return;
        try {
            saleDao.update(readSaleForm(sales.get(row).getId()));
            refreshItems();
            refreshSales();
        } catch (IllegalArgumentException e) {
            ViewUtils.showError(view, e.getMessage());
        } catch (SQLException e) {
            showSqlError(e);
        }
    }

    private void deleteSale() {
        int row = requireSelectedRow(view.getSalesPanel(), "продажу");
        if (row < 0) return;
        Sale sale = sales.get(row);
        if (!ViewUtils.confirm(view, "Удалить продажу товара «" + sale.getItemName() + "»?")) return;
        runDatabaseAction(() -> {
            saleDao.delete(sale.getId());
            refreshItems();
            refreshSales();
        });
    }

    private void refreshSalePriceCombo(Integer selectedPriceId) {
        Item item = (Item) view.getSalesPanel().getItemCombo().getSelectedItem();
        JComboBox<Price> combo = view.getSalesPanel().getPriceCombo();
        combo.removeAllItems();
        if (item == null) return;
        for (Price price : prices) {
            if (price.getItemId() == item.getId()) {
                combo.addItem(price);
                if (selectedPriceId != null && price.getId() == selectedPriceId) {
                    combo.setSelectedItem(price);
                }
            }
        }
    }

    private void buildReport() {
        try {
            ReportPanel panel = view.getReportPanel();
            LocalDate from = ValidationUtils.parseDate(panel.getFromField().getText(), "Дата начала");
            LocalDate to = ValidationUtils.parseDate(panel.getToField().getText(), "Дата окончания");
            if (to.isBefore(from)) throw new IllegalArgumentException("Конец периода раньше начала");
            List<FinancialReportRow> rows = reportDao.findForPeriod(from, to);
            panel.getTableModel().setRowCount(0);
            BigDecimal totalLoans = BigDecimal.ZERO;
            BigDecimal totalCommission = BigDecimal.ZERO;
            for (FinancialReportRow row : rows) {
                panel.getTableModel().addRow(new Object[]{row.getContractId(), row.getClientName(),
                        row.getIssueDate(), row.getReturnDueDate(), row.getLoanAmount(),
                        row.getCommissionAmount(), row.getTotalAssessedValue(), row.getItemCount()});
                totalLoans = totalLoans.add(row.getLoanAmount());
                totalCommission = totalCommission.add(row.getCommissionAmount());
            }
            panel.setReportSummary(rows.size(), totalLoans, totalCommission);
        } catch (IllegalArgumentException e) {
            ViewUtils.showError(view, e.getMessage());
        } catch (SQLException e) {
            showSqlError(e);
        }
    }

    private int requireSelectedRow(BaseTablePanel panel, String objectName) {
        int row = panel.getSelectedModelRow();
        if (row < 0) {
            ViewUtils.showError(view, "Сначала выберите " + objectName + " в таблице");
        }
        return row;
    }

    private <T> void refillCombo(JComboBox<T> combo, List<T> values) {
        combo.removeAllItems();
        for (T value : values) combo.addItem(value);
    }

    private void selectItem(JComboBox<Item> combo, int itemId) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getId() == itemId) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    /** Добавляет товар в форму только для просмотра или изменения старой записи. */
    private void ensureItemPresent(JComboBox<Item> combo, int itemId) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getId() == itemId) return;
        }
        for (Item item : items) {
            if (item.getId() == itemId) {
                combo.addItem(item);
                return;
            }
        }
    }

    private LocalDate requireItemReturnDueDate(int itemId) throws SQLException {
        return contractDao.findCurrentReturnDueDateForItem(itemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Сначала добавьте товар в состав договора"
                ));
    }

    private void validateStatusChange(Item item, ItemStatus newStatus) throws SQLException {
        ItemStatus oldStatus = item.getStatus();
        if (oldStatus == newStatus) return;

        if (oldStatus == ItemStatus.PLEDGED && newStatus == ItemStatus.RETURNED) {
            requireItemReturnDueDate(item.getId());
            return;
        }
        if (oldStatus == ItemStatus.PLEDGED && newStatus == ItemStatus.OWNED_BY_PAWNSHOP) {
            requireDeadlinePassed(item.getId());
            return;
        }
        if (oldStatus == ItemStatus.RETURNED && newStatus == ItemStatus.PLEDGED) {
            throw new IllegalArgumentException(
                    "Для повторного залога добавьте возвращённый товар в состав нового договора"
            );
        }
        if (oldStatus == ItemStatus.SOLD) {
            throw new IllegalArgumentException(
                    "Для отмены статуса проданного товара сначала удалите запись о продаже"
            );
        }
        if (oldStatus == ItemStatus.OWNED_BY_PAWNSHOP) {
            throw new IllegalArgumentException(
                    "Собственность ломбарда можно только продать; статус изменится автоматически"
            );
        }
        if (newStatus == ItemStatus.SOLD) {
            throw new IllegalArgumentException(
                    "Статус «Продан» устанавливается автоматически при создании продажи"
            );
        }
        throw new IllegalArgumentException("Такой переход статуса товара недопустим");
    }

    /** Передача товара ломбарду возможна только на следующий день после срока возврата. */
    private void requireDeadlinePassed(int itemId) throws SQLException {
        LocalDate dueDate = requireItemReturnDueDate(itemId);
        if (!LocalDate.now().isAfter(dueDate)) {
            throw new IllegalArgumentException(
                    "Срок возврата ещё не истёк. Передать товар ломбарду можно с "
                            + dueDate.plusDays(1)
            );
        }
    }

    /** Дата цены или продажи также должна быть позже срока возврата. */
    private void requireOperationAfterDeadline(int itemId, LocalDate operationDate,
                                               String fieldName) throws SQLException {
        LocalDate dueDate = requireItemReturnDueDate(itemId);
        if (!LocalDate.now().isAfter(dueDate)) {
            throw new IllegalArgumentException(
                    "Срок возврата ещё не истёк. Операция будет доступна с "
                            + dueDate.plusDays(1)
            );
        }
        if (!operationDate.isAfter(dueDate)) {
            throw new IllegalArgumentException(
                    fieldName + " должна быть не раньше " + dueDate.plusDays(1)
            );
        }
    }

    private boolean isAdmin() {
        return currentUser.getRole() == Role.ADMIN;
    }

    private void logout() {
        view.dispose();
        DatabaseConnection.getInstance().close();
        logoutAction.run();
    }

    private void runDatabaseAction(SqlAction action) {
        try {
            action.run();
        } catch (SQLException e) {
            showSqlError(e);
        }
    }

    private void showSqlError(SQLException e) {
        ViewUtils.showError(view, sqlMessage(e));
    }

    private String sqlMessage(SQLException e) {
        if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
            return "Операция нарушает связи или уникальность данных.\n" + e.getMessage();
        }
        return "Ошибка базы данных:\n" + e.getMessage();
    }

    @FunctionalInterface
    private interface SqlAction {
        void run() throws SQLException;
    }
}
