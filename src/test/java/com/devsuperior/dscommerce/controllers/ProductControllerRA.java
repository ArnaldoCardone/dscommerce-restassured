package com.devsuperior.dscommerce.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

public class ProductControllerRA {
    
    private Long existingProductId, nonExistingProductId;
    
    @BeforeEach
    public void setUp(){
        //Define a URL e o endpoint base
        baseURI = "http://localhost:8080";
        //basePath = "/products";
    }

    @Test
    public void findByIdShoulhReturnProductWhenIdExists(){

        existingProductId = 2L;

        given()
            .get("/products/{id}", existingProductId)
        .then()
          .statusCode(200)
          .body("id", is(existingProductId.intValue()))
          .body("name", equalTo("Smart TV"))
          .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg"))
          .body("price", is(2190.0F))
          .body("categories.id", hasItems(2,3))
          .body("categories.name", hasItems("Eletrônicos", "Computadores"));
    }

    @Test
    public void findAllShoulhReturnPageProductsWhenProductNameIsEmpty(){

         //Busca paginada inclui o content. para acessar o campo;
        given()
            .get("/products")
        .then()
          .statusCode(200)
          .body("content.name", hasItems("PC Gamer Tera", "Macbook Pro"));
    }

    @Test
    public void findAllShoulhReturnPageProductsWhenProductNameIsNotEmpty(){
        String productName = "Macbook Pro";

        //Para acessar o campo de um objeto dentro de uma lista, é necessário informar o índice do objeto;
        given()
            .get("/products?name={productName}",productName)
        .then()
          .statusCode(200)
          .body("content.id[0]", is(3))
          .body("content.name[0]", equalTo( "Macbook Pro"))
          .body("content.price[0]", is(1250.0F));
    }

    @Test
    public void findAllShoulhReturnPageProductsWhenProductNameIsNotEmptyAndPriceGreater2000(){

        //Verifica na lista retornada, se existe algum produto com preço maior que 2000;
        given()
            .get("/products")
        .then()
          .statusCode(200)
          .body("content.findAll {it.price > 2000}.name", hasItems("PC Gamer Weed", "Smart TV"));
    }
}
