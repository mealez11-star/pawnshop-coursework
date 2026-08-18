package ru.mospolytech.pawnshop.model;

public class Client {
    private int id;
    private String fullName;
    private String passportData;
    private int userId;

    public Client(int id, String fullName, String passportData, int userId) {
        this.id = id;
        this.fullName = fullName;
        this.passportData = passportData;
        this.userId = userId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPassportData() { return passportData; }
    public void setPassportData(String passportData) { this.passportData = passportData; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    @Override
    public String toString() {
        return id + " - " + fullName;
    }
}
