package net.hasor.dbvisitor.test.oneapi.model.types;

import java.util.List;
import java.util.Objects;

/**
 * JSON 类型测试 Bean
 * 用于测试 JSON TypeHandler 的序列化和反序列化功能
 */
public class JsonTestBean {
    private String       name;
    private Integer      age;
    private Boolean      active;
    private List<String> tags;
    private Address      address;

    public JsonTestBean() {
    }

    public JsonTestBean(String name, Integer age, Boolean active) {
        this.name = name;
        this.age = age;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JsonTestBean that = (JsonTestBean) o;
        return Objects.equals(name, that.name) && Objects.equals(age, that.age) && Objects.equals(active, that.active) && Objects.equals(tags, that.tags) && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, active, tags, address);
    }

    /**
     * 嵌套地址对象 - 用于测试嵌套 JSON
     */
    public static class Address {
        private String city;
        private String street;
        private String zipCode;

        public Address() {
        }

        public Address(String city, String street, String zipCode) {
            this.city = city;
            this.street = street;
            this.zipCode = zipCode;
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

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Address address = (Address) o;
            return Objects.equals(city, address.city) && Objects.equals(street, address.street) && Objects.equals(zipCode, address.zipCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(city, street, zipCode);
        }
    }
}
