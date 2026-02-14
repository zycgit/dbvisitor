package net.hasor.dbvisitor.test.model.complex;

/**
 * Level 5: Nested object for ComplexOrder
 */
public class Address {
    private String province;
    private String city;
    private String street;
    private String zipCode;

    public Address() {
    }

    public Address(String province, String city, String street, String zipCode) {
        this.province = province;
        this.city = city;
        this.street = street;
        this.zipCode = zipCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}
