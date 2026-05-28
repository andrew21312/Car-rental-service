package com.car_rental.entity;

import java.util.Objects;

public class Role {
    private int id;
    private String roleName;
    private String description;

    public Role() {
    }

    public Role(int roleId, String roleName, String description) {
        this.id = roleId;
        this.roleName = roleName;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int roleId) {
        this.id = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormattedName() {
        return roleName.replace("_", " ");
    }

    public String getLowercaseName() {
        return roleName.toLowerCase();
    }

    public String getNameCapitalized() {
        this.roleName = roleName.replace("_", " ");
        return roleName.substring(0, 1).toUpperCase() + roleName.substring(1).toLowerCase();
    }


    @Override
    public String toString() {
        return "Role{" +
               "id=" + id +
               ", roleName='" + roleName + '\'' +
               ", role_description='" + description + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return id == role.id && Objects.equals(roleName, role.roleName) &&
               Objects.equals(description, role.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, roleName, description);
    }
}
