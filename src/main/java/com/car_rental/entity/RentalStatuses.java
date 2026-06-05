package com.car_rental.entity;

import java.util.Objects;

public class RentalStatuses {
    private int id;
    private String name;

    public RentalStatuses() {
    }

    public RentalStatuses(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormattedName() {
        return name.replace("_", " ");
    }

    public String getLowercaseName() {
        return name.toLowerCase();
    }

    public String getNameCapitalized() {
        this.name = name.replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    @Override
    public String toString() {
        return "RentalStatuses{" +
               "id=" + id +
               ", name='" + name + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RentalStatuses that = (RentalStatuses) o;
        return id == that.id && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
