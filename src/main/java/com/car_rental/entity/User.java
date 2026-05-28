package com.car_rental.entity;


import com.car_rental.form.user.UserUpdateDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


public class User extends UserUpdateDTO {
    public static final String DELETED_USERNAME = "deleted_user";
    public static final String DELETED = "Deleted";

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    private Timestamp createdAt;

    public User() {
    }

    public User(String username, String firstName, String lastName, String password, Role role,
                String phoneNumber) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.role = role;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.phoneNumber = phoneNumber;
    }

    // To be used when updating a user
    public User(UserUpdateDTO userUpdateDTO) {
        this.id = userUpdateDTO.getId();
        this.username = userUpdateDTO.getUsername();
        this.firstName = userUpdateDTO.getFirstName();
        this.lastName = userUpdateDTO.getLastName();
        this.role = userUpdateDTO.getRole();
        this.phoneNumber = userUpdateDTO.getPhoneNumber();
    }

    public void markAsDeleted() {
        this.setUsername(DELETED_USERNAME);
        this.setFirstName(DELETED);
        this.setLastName(DELETED);
        this.setPassword(DELETED);
        this.setRole(null);
        this.setPhoneNumber(DELETED);
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String passwordHash) {
        this.password = passwordHash;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return dateFormat.format(createdAt);
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", password='" + password + '\'' +
               ", role=" + role +
               ", createdAt=" + createdAt +
               ", phoneNumber='" + phoneNumber + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
