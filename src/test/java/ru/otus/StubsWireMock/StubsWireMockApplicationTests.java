package ru.otus.StubsWireMock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static io.restassured.RestAssured.given;


@SpringBootTest
class StubsWireMockApplicationTests {

	private static WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().port(5050));

	@BeforeAll
	public static void setUpServer(){
		wireMockServer.start();

		//Заглушка для GET SINGLE USER
		WireMock.configureFor("localhost",5050);
		WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/users/2"))
				.willReturn(WireMock.aResponse()
						.withStatus(200)
						.withBody("{\n" +
								"    \"data\": {\n" +
								"        \"id\": 2,\n" +
								"        \"email\": \"janet.weaver@reqres.in\",\n" +
								"        \"first_name\": \"Janet\",\n" +
								"        \"last_name\": \"Weaver\",\n" +
								"        \"avatar\": \"https://reqres.in/img/faces/2-image.jpg\"\n" +
								"    },\n" +
								"    \"support\": {\n" +
								"        \"url\": \"https://reqres.in/#support-heading\",\n" +
								"        \"text\": \"To keep ReqRes free, contributions towards server costs are appreciated!\"\n" +
								"    }\n" +
								"}")));

		//Заглушка для GET SINGLE USER NOT FOUND
		WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/users/23"))
				.willReturn(WireMock.aResponse()
				.withStatus(404)));

		//Заглушка для POST CREATE USER
		WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/api/users"))
				.willReturn(WireMock.aResponse()
				.withStatus(201)
				.withBody("{\n" +
						"    \"name\": \"morpheus\",\n" +
						"    \"job\": \"leader\",\n" +
						"    \"id\": \"" + getRandomID() + "\",\n" +
						"    \"createdAt\": \"" + currentDate() + "\"\n" +
						"}")));

		//Заглушка для DELETE USER
		WireMock.stubFor(WireMock.delete(WireMock.urlEqualTo("/api/users/2"))
				.willReturn(WireMock.aResponse()
				.withStatus(204)));
	}

	@Test
	@DisplayName("GET SINGLE USER INFO")
	void checkSingeUserTest() {
		Response response = given()
				.contentType(ContentType.JSON)
				.when()
				.get("http://localhost:5050/api/users/2")
				.then()
				.extract().response();

		response.getBody().prettyPrint();

		Assertions.assertEquals(200, response.statusCode());
		Assertions.assertEquals("Janet", response.jsonPath().getString("data.first_name"));
		Assertions.assertEquals("Weaver", response.jsonPath().getString("data.last_name"));
	}

	@Test
	@DisplayName("GET SINGLE USER NOT FOUND")
	void checkSingleUserNotFound(){
		Response response = given()
				.contentType(ContentType.JSON)
				.when()
				.get("http://localhost:5050/api/users/23")
				.then()
				.extract().response();

		response.getBody().prettyPrint();

		Assertions.assertEquals(404, response.statusCode());
	}

	@Test
	@DisplayName("POST CREATE USER")
	void checkUserCreation(){
		Response response = given()
				.contentType(ContentType.JSON)
				.when()
				.body("{\n" +
						"    \"name\": \"Morpheus\",\n" +
						"    \"job\": \"Leader\"\n" +
						"}")
				.post("http://localhost:5050/api/users")
				.then()
				.extract().response();

		response.getBody().prettyPrint();

		Assertions.assertEquals(201, response.statusCode());
		Assertions.assertEquals("morpheus", response.jsonPath().getString("name"));
		Assertions.assertEquals("leader", response.jsonPath().getString("job"));
	}

	@Test
	@DisplayName("DELETE USER")
	void checkDeleteUser(){
		Response response = given()
				.contentType(ContentType.JSON)
				.when()
				.delete("http://localhost:5050/api/users/2")
				.then()
				.extract().response();

		response.getBody().prettyPrint();

		Assertions.assertEquals(204, response.statusCode());
	}

	@AfterAll
	public static void tearDownMockServer(){
		wireMockServer.stop();
	}

	//Генерация случайного ID для заглушки
	private static int getRandomID(){
		return (int)(Math.random() * 1000);
	}

	//Получение текущей даты
	private static Date currentDate(){
		return new Date();
	}
}