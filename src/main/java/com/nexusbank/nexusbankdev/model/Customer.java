package com.nexusbank.nexusbankdev.model;

import jakarta.persistence.*;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long customerId; //Long is used because it can store much larger numbers than int.
    // 'Long' is used instead of 'long' because only 'Long' can contain null values.

    private String firstName;
    private String lastName;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    @Column(name = "phone")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(unique = true)
    private String email;

    private String nic;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    // 'WRITE_ONLY' represents that the customer can send data to the backend,
    // but backend will not send any data to the customer back.
    private String passwordHash; //Therefore when the customer detials are requested the passwrodHash will not include in the details
    private String kycStatus; // PENDING, APPROVED, REJECTED
    private String accountStatus; // ACTIVE, FROZEN, SUSPENDED
    private String trackingId;
    private String registrationDate;
    private String timestamp;

    public Customer() {
        this.timestamp = Instant.now().toString();
        this.kycStatus = "PENDING";
        this.accountStatus = "ACTIVE";
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.fullName = (firstName != null ? firstName : "") + " " + (this.lastName != null ? this.lastName : "");
        //this will automatically update if the first name is changed
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.fullName = (this.firstName != null ? this.firstName : "") + " " + (lastName != null ? lastName : "");
        //this will automatically update if the last name is changed
    }

    public String getFullName() {
        return fullName != null ? fullName : (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        //first if the full name is already existing it will return the full name
        //if not it will generate the full name using the firs name and the last name and return it
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhone() {
        return phoneNumber;
    }

    public void setPhone(String phone) {
        this.phoneNumber = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getStreet() {
        return address;
    }

    public void setStreet(String street) {
        this.address = street;
    }

    public String getCity() {
        return "";
    }

    public void setCity(String city) {
        if (city != null && !city.isEmpty() && this.address != null && !this.address.contains(city)) {
            this.address = this.address + ", " + city;
        }
    }

    public String getPostalCode() {
        return "";
    }

    public void setPostalCode(String postalCode) {
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
