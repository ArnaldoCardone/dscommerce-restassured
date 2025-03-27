package com.devsuperior.dscommerce.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import com.devsuperior.dscommerce.tests.TokenUtil;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;


public class ProductControllerRA {
    
    private Long existingProductId, nonExistingProductId;
    private String clientUserName, clientPassword, adminUserName, adminPassword;
    private Map<String, Object> postProductInstance;
    private String adminToken, clientToken, invalidToken;

    @BeforeEach
    public void setUp() {
        //Define a URL e o endpoint base
        baseURI = "http://localhost:8080";
     
        //Instancia um produto para ser utilizado no método POST, simulando o Json enviado no corpo da requisição;
        postProductInstance = new HashMap<>();
        postProductInstance.put("name", "Meu Produto");
        postProductInstance.put("description", "Descrição do produto");
        postProductInstance.put("price", 100.0);
        postProductInstance.put("imgUrl", "https://img.com/img.jpg");
        //Adiciona as categorias do produto;
        List<Map<String, Object>> categories = new ArrayList<>();
        Map<String, Object> category1 = new HashMap<>();
        category1.put("id", 2);
        Map<String, Object> category2 = new HashMap<>();
        category2.put("id", 3);
        categories.add(category1);
        categories.add(category2);
        //Adiciona a lista de categorias ao produto;
        postProductInstance.put("categories", categories);

        clientUserName = "maria@gmail.com";
        clientPassword = "123456";
        adminUserName = "alex@gmail.com";
        adminPassword = "123456";

        adminToken = new TokenUtil().obtainAccesToken(adminUserName, adminPassword);
        clientToken = new TokenUtil().obtainAccesToken(clientUserName, clientPassword);
        invalidToken = adminToken + "invalid";
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
    public void findByIdShoulhReturnNotFoundWhenIdDoesNotExists(){

        nonExistingProductId = 2000L;

        given()
            .get("/products/{id}", nonExistingProductId)
        .then()
          .statusCode(404);
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

    @Test
	public void insertShouldReturnProductCreatedWhenLoggedAsAdmin()  throws JSONException{

		JSONObject newProduct = new JSONObject(postProductInstance);

		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(201)
			.body("name", equalTo("Me 123"))
			.body("price", is(20.0f))
			.body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
			.body("categories.id", hasItems(2, 3));
	}

    @Test
	public void insertShouldReturnForbiddendWhenLoggedAsClient()  throws JSONException{
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(403);
    }

    @Test
    public void insertShouldReturnUnathorizedWhenLoggedAsClient()  throws JSONException{
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + invalidToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(401);
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminsIsLoggedAdndInvalidName(){
        //Seta o nome para gerar erro de validação;
        postProductInstance.put("name", "Me");
        JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422)
            .body("errors.message[0]", equalTo("Nome precisar ter de 3 a 80 caracteres"));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminsIsLoggedAndPriceNegative(){
        //Seta o nome para gerar erro de validação;
        postProductInstance.put("price", -50.0F);
        JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422)
            .body("errors.message[0]", equalTo("O preço deve ser positivo"));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminsIsLoggedAndCategorieDoesNotExists(){
        //Seta o nome para gerar erro de validação;
        postProductInstance.put("categories", null);
        JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422)
            .body("errors.message[0]", equalTo("Deve ter pelo menos uma categoria"));
    }

}
