package com.felipe.dscommerce.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

import io.restassured.http.ContentType;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductControllerRA {

    private Long existingProductId, nonExistingProductId;
    private String productName;
    private Map<String, Object> postProductInstance;
    @BeforeEach
    public void setup() throws Exception {
        baseURI = "http://localhost:8080";

        productName = "Macbook";

        postProductInstance = new HashMap<>();
        postProductInstance.put("name", "Meu produto");
        postProductInstance.put("description",  "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim");
        postProductInstance.put("imgUrl", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
        postProductInstance.put("price", 50.0);

        List<Map<String, Object>> categories = new ArrayList<>();
        categories.add(new HashMap<String, Object>() {{ put("id", 2); }});
        categories.add(new HashMap<String, Object>() {{ put("id", 3); }});
        postProductInstance.put("categories", categories);
    }

    @Test
    public void findByIdShouldReturnProductWhenIdExists() {
        existingProductId = 2L;

        given()
                .get("/products/{id}", existingProductId)
        .then()
                .statusCode(200)
                .body("id", is(2))
                .body("name", equalTo("Smart TV"))
                .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg"))
                .body("price", is(2190.0F))
                .body("categories.id", hasItems(2, 3))
                .body("categories.name", hasItems("Eletrônicos", "Computadores"))
        ;

    }

    @Test
    public void findAllShouldReturnPageProductsWhenProductNameIsEmpty() {
        given()
                .get("/products?page=0")
        .then()
                .statusCode(200)
                .body("content.name", hasItems("Macbook Pro", "PC Gamer Tera"))
        ;
    }

    @Test
    public void findAllShouldReturnPageProductsWhenProductNameIsNotEmpty() {
            given()
                .get("/products?name={productName}", productName)
            .then()
                .statusCode(200)
                .body("content.id[0]", is(3))
                .body("content.name[0]", equalTo("Macbook Pro"))
                .body("content.price[0]", is(1250.0f))
                .body("content.imgUrl[0]", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/3-big.jpg"))
            ;
    }

    @Test
    public void findAllShouldReturnPagedProductsWhithPriceGreaterThan2000() {
        given()
            .get("/products?size=25")
        .then()
            .statusCode(200)
            .body("content.findAll { it.price > 2000 }.name", hasItems("PC Gamer Weed", "Smart TV", "PC Gamer Min"))
        ;
    }

    @Test
    public void insertShouldReturnProductCreatedWhenAdminLogged() {
        JSONObject newProduct = new JSONObject(postProductInstance);
        given()
            .header("Content-Type", "application/json")
                .header("Authorization", "Bearer" + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
            .when()
                .post("/products")
            .then()
                .statusCode(200)
            .body("name", equalTo("Meu produto"))
            .body("price", is(50.0F))
            .body("imgUrl", equalTo("imgUrl\", \"https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
            .body("categories.id", hasItems(2, 3))
        ;
    }
}
