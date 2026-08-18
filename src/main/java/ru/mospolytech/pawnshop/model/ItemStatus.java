package ru.mospolytech.pawnshop.model;

public enum ItemStatus {
    PLEDGED("В залоге"),
    RETURNED("Возвращён клиенту"),
    OWNED_BY_PAWNSHOP("Собственность ломбарда"),
    SOLD("Продан");

    private final String displayName;

    ItemStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
