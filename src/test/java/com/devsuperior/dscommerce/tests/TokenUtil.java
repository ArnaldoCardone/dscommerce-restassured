package com.devsuperior.dscommerce.tests;

import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

public class TokenUtil {


    public String obtainAccesToken(String username, String password) {
        //Extrai o token do corpo da resposta para usarmos nas chamadas dos testes
        Response response = authRequest(username, password);
        return response.jsonPath().getString("access_token");
    }

    private static Response authRequest(String username, String password) {
        //Faz a requisição para realizar o login na aplicação e recuperar o token
        return given()
                .auth()
                .preemptive()
                .basic("myclientid", "myclientsecret")
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("username", username)
                .formParam("password", password)
                .when()
                .post("/oauth2/token");
    }
}
