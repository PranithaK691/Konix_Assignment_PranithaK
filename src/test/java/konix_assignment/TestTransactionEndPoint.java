package konix_assignment;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.log4j.Logger;
import org.example.Main;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

public class TestTransactionEndPoint{
    private final static Logger logger = Logger.getLogger(Main.class);
    private final String coin1 = "INR";
    private final String coin2 = "USDT";

    // Generate random coin1 and coin2
    Random random = new Random();
    private int coin1Amount =  random.nextInt(1,1000);
    private int coin2Amount =  random.nextInt(1,1000);
    private int id = 0;
    private LocalDateTime createdAt =  LocalDateTime.now();
    @Test
    public void testTransactionPOSTRoute() throws ParseException {
        // RESTAssured base URI
        baseURI = "https://x8ki-letl-twmt.n7.xano.io/api:gHPd8le5/transaction";

        //disable URL encoding to escape colon symbol encoding in baseURI
        RequestSpecification mySpec = new RequestSpecBuilder().setUrlEncodingEnabled(false).build();

        // build the request body
        JSONObject request = new JSONObject();
        request.put("coin1", coin1);
        request.put("coin2", coin2);
        request.put("coin1Amount",coin1Amount);
        request.put("coin2Amount", coin2Amount);

        //LOGGING
        logger.info("baseURI: "+baseURI);
        logger.info("request: "+request.toJSONString());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = now.plusSeconds(10);

        // Making API call and asserting the actual results with expected results
        id = given().spec(mySpec)
                .contentType(ContentType.JSON).accept(ContentType.JSON)
                .body(request.toJSONString())
                .when().post()
                .then().statusCode(200)
                .and().body("sentCoin", equalTo(coin1))
                .and().body("receivedCoin", equalTo(coin2))
                .and().body("sentCoinAmount", equalTo(coin1Amount))
                .and().body("receivedCoinAmount", equalTo(coin2Amount))
        .extract().body().path("id");
        logger.info("POST API id: "+id);
    }

    @Test (dependsOnMethods = "testTransactionPOSTRoute")
    public void testTransactionGETRoute() throws InterruptedException {
        // RESTAssured base URI
        baseURI = "https://x8ki-letl-twmt.n7.xano.io/api:gHPd8le5/transaction/";

        //disable URL encoding to escape colon symbol encoding in baseURI
        RequestSpecification mySpec = new RequestSpecBuilder().setUrlEncodingEnabled(false).build();

        ValidatableResponse getApiResponse = given().spec(mySpec).get(String.valueOf(id))
                .then().statusCode(200)
                .body("id", equalTo(id))
                .body("sentCoin", equalTo(coin1))
                .body("receivedCoin", equalTo(coin2))
                .body("sentCoinAmount", equalTo(coin1Amount))
                .body("receivedCoinAmount", equalTo(coin2Amount))
                .body("receivedCoinMarketPrice", equalTo((float) coin1Amount / coin2Amount));

        long timestamp = getApiResponse.extract().body().path("created_at");
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime createdTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        assertEquals(createdTime.getYear(), createdAt.getYear());
        assertEquals(createdTime.getMonth(), createdAt.getMonth());
        assertEquals(createdTime.getHour(), createdAt.getHour());
        assertEquals(createdTime.getMinute(), createdAt.getMinute());
    }
}
