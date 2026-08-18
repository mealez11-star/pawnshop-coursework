package ru.mospolytech.pawnshop.model;

public class User {
    private int id;
    private String login;
    private String passwordHash;
    private String fullName;
    private Role role;

    public User(int id, String login, String passwordHash, String fullName, Role role) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
