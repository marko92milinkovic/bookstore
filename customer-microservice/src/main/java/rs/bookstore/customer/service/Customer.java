/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.customer.service;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 *
 * @author marko
 */
@DataObject(generateConverter = true)
public class Customer {

    private String id;
    private String username;
    private String phone;
    private String email;
    private Long birthDate;

    public Customer() {
    }
    
    public Customer(JsonObject json) {
        CustomerConverter.fromJson(json, this);
    }
    
    public JsonObject toJson () {
        JsonObject json = new JsonObject();
        CustomerConverter.toJson(this, json);
        return json;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Long birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public String toString() {
        return "Customer{" + "id=" + id + ", username=" + username + ", phone=" + phone + ", email=" + email + ", birthDate=" + birthDate + '}';
    }
    
    
}
