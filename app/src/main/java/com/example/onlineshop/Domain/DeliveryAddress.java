// com.example.onlineshop.domain.DeliveryAddress.java
package com.example.onlineshop.Domain;

public class DeliveryAddress {
    private String fullName;
    private String street;
    private String city;
    private String state;
    private String pinCode;
    private String country;

    public DeliveryAddress(String fullName, String street, String city, String state, String pinCode, String country) {
        this.fullName = fullName;
        this.street = street;
        this.city = city;
        this.state = state;
        this.pinCode = pinCode;
        this.country = country;
    }

    // Getters
    public String getFullName() { return fullName; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPinCode() { return pinCode; }
    public String getCountry() { return country; }

    // Formatted full address (for Receipt display)
    public String getFormattedAddress() {
        return fullName + "\n" + street + "\n" +
                city + ", " + state + " - " + pinCode + "\n" + country;
    }
}
