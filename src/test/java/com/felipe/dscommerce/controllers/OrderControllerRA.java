package com.felipe.dscommerce.controllers;

import com.felipe.dscommerce.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

public class OrderControllerRA {

    private String clientUsername, clientPassword, adminUsername, adminPassword;
    private String clientToken, adminToken, invalidToken;
    private Long existingOrderId, nonExistingOrderId, dependentOrderId;

    @BeforeEach
    public void setup() throws Exception {
        baseURI = "http://localhost:8080";

        existingOrderId = 1L;
        nonExistingOrderId = 1000L;
        dependentOrderId = 4L;

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
        adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
        invalidToken = adminToken + "abc";
    }

    @Test
    public void findByIdShouldReturnOrderWhenIdExistsAndAdminLogged() {
        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
        .when()
                .get("/orders/{id}", existingOrderId)
        .then()
                .statusCode(200)
                .body("id", is(1))
                .body("moment", is("2022-07-25T13:00:00Z"))
                .body("status", equalTo("PAID"))
                .body("client.name", equalTo("Maria Brown"))
                .body("payment.moment", is("2022-07-25T15:00:00Z"))
                .body("items.size()", is(2))
                .body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"))
                .body("total", is(1431.0F))
        ;
        ;

    }
}
