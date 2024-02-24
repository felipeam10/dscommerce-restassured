package com.felipe.dscommerce.controllers;

import com.felipe.dscommerce.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

public class OrderControllerRA {

    private String clientUsername, clientPassword, adminUsername, adminPassword, adminOnlyUsername, adminOnlyPassword;
    private String clientToken, adminToken, invalidToken, adminOnlyToken;
    private Long existingOrderId, nonExistingOrderId, dependentOrderId;
    private Map<String, List<Map<String, Object>>> postOrderInstance;

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
        adminOnlyUsername = "ana@gmail.com";
        adminOnlyPassword = "123456";

        clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
        adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
        adminOnlyToken = TokenUtil.obtainAccessToken(adminOnlyUsername, adminOnlyPassword);
        invalidToken = adminToken + "abc";

        Map<String, Object> item1 = new HashMap<>();
        item1.put("productId", 1);
        item1.put("quantity", 2);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("productId", 5);
        item2.put("quantity", 1);

        List<Map<String,Object>> itemInstances = new ArrayList<>();
        itemInstances.add(item1);
        itemInstances.add(item2);

        postOrderInstance = new HashMap<>();
        postOrderInstance.put("items", itemInstances);
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
    }

    @Test
    public void findByIdShouldReturnOrderWhenIdExistsAndClientLogged() {
        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
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
    }

    @Test
    public void findByIdShouldReturnForbiddenWhenIdExistsAndClientLoggedAndOrderDoesNotBelongUser() {
        Long otherOrderId = 2L;
        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
        .when()
                .get("/orders/{id}", otherOrderId)
        .then()
                .statusCode(403)
        ;
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExistsAndAdminLogged() {
        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
        .when()
                .get("/orders/{id}", nonExistingOrderId)
        .then()
                .statusCode(404)
        ;
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExistsAndClientLogged() {
        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
        .when()
                .get("/orders/{id}", nonExistingOrderId)
        .then()
                .statusCode(404)
        ;
    }

    @Test
    public void findByIdShouldReturnUnauthorizedWhenIdExistsAndInvalidToken() {
        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .accept(ContentType.JSON)
        .when()
                .get("/orders/{id}", existingOrderId)
        .then()
                .statusCode(401)
        ;
    }

    @Test
    public void insertShouldReturnOrderCreatedWhenClientLogged() throws JSONException {
        JSONObject newOrder = new JSONObject(postOrderInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(newOrder)
        .when()
                .post("/orders")
        .then()
                .statusCode(201)
                .body("status", equalTo("WAITING_PAYMENT"))
                .body("client.name", equalTo("Maria Brown"))
                .body("items.name", hasItems("The Lord of the Rings", "Rails for Dummies"))
                .body("total", is(281.99F));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenClientLoggedAndOrderHasNoItem() throws Exception {
        postOrderInstance.put("items", null);
        JSONObject newOrder = new JSONObject(postOrderInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(newOrder)
        .when()
                .post("/orders")
        .then()
                .statusCode(422);
    }

    @Test
    public void insertShouldReturnForbiddenWhenAdminLogged() throws JSONException {
        JSONObject newOrder = new JSONObject(postOrderInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminOnlyToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(newOrder)
        .when()
                .post("/orders")
        .then()
                .statusCode(403);
    }

    @Test
    public void insertShouldReturnUnauthorizedWhenInvalidToken() throws JSONException {
        JSONObject newOrder = new JSONObject(postOrderInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(newOrder)
        .when()
                .post("/orders")
        .then()
                .statusCode(401);
    }
}
