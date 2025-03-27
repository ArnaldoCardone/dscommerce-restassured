package com.devsuperior.dscommerce.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import com.devsuperior.dscommerce.tests.TokenUtil;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

public class OrderControllerRA {

    private Long existingOrderId, nonExistingOrderId, dependentOrderId;
    private String clientUserName, clientPassword, adminUserName, adminPassword;
    private String adminToken, clientToken, invalidToken;

    @BeforeEach
    public void setUp() {
        //Define a URL e o endpoint base
        baseURI = "http://localhost:8080";

        clientUserName = "maria@gmail.com";
        clientPassword = "123456";
        adminUserName = "alex@gmail.com";
        adminPassword = "123456";

        adminToken = new TokenUtil().obtainAccesToken(adminUserName, adminPassword);
        clientToken = new TokenUtil().obtainAccesToken(clientUserName, clientPassword);
        invalidToken = adminToken + "invalid";

        existingOrderId = 1L;
        nonExistingOrderId = 1000L;
    }

    @Test
    public void findByIdShoulhReturnOrderWhenAdminIsLoggedIdExists() {
        existingOrderId = 1L;
        adminUserName = "alex@gmail.com";
        adminPassword = "123456";

        adminToken = new TokenUtil().obtainAccesToken(adminUserName, adminPassword);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + adminToken)
            .accept(ContentType.JSON)
        .when()
            .get("/orders/{id}", existingOrderId)
        .then()
            .statusCode(200)
            .body("id", is(existingOrderId.intValue()))
            .body("status", equalTo("PAID"))
            .body("client.id", is(1))
            .body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"))
            .body("total", is(1431.0f));
    }

    @Test
    public void findByIdShoulhReturnOrderWhenClientIsLoggedAndOrderClientIsOwner() {
        //Seta para um pedido do usuário Client
        existingOrderId = 2L;
        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + clientToken)
            .accept(ContentType.JSON)
        .when()
            .get("/orders/{id}", existingOrderId)
        .then()
            .statusCode(200)
            .body("id", is(existingOrderId.intValue()))
            .body("status", equalTo("DELIVERED"))
            .body("client.id", is(2))
            .body("items.name", hasItems( "Macbook Pro"))
            .body("total", is(1250.0f));
    }

    @Test
    public void findByIdShoulhReturnForbiddenWhenClientIsLoggedAndOrderDoesNotBelong() {
        
        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + clientToken)
            .accept(ContentType.JSON)
        .when()
            .get("/orders/{id}", existingOrderId)
        .then()
            .statusCode(403);
    }

    @Test
    public void findByIdShoulhReturnNotFoundWhenAdminIsLoggedAndOrderDoesNotExists() {
        
        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + adminToken)
            .accept(ContentType.JSON)
        .when()
            .get("/orders/{id}", nonExistingOrderId)
        .then()
            .statusCode(404);
    }

    @Test
    public void findByIdShoulhReturnUnathorizedWhenInvalidToken() {
        
        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + invalidToken)
            .accept(ContentType.JSON)
        .when()
            .get("/orders/{id}", nonExistingOrderId)
        .then()
            .statusCode(401);
    }

}
