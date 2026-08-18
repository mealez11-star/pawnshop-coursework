package ru.mospolytech.pawnshop.model;

public class Item {
    private int id;
    private String name;
    private ItemStatus status;

    public Item(int id, String name, ItemStatus status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
