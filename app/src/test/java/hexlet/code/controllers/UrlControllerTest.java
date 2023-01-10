package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.HttpCode;
import kong.unirest.Unirest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static hexlet.code.App.getApp;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class UrlControllerTest {
    private static final int PORT = 5002;
    private static final String HOST = "http://localhost";
    private static final String BASE_URL = HOST + ":" + PORT;

    @BeforeAll
    static void beforeAll() {
        var app = getApp();
        app.start(PORT);

        // create sample data
        new Url("https://google.com").save();
        new Url("https://apple.com").save();
        new Url("https://facebook.com").save();
        new Url("https://my-site.com:8080").save();
    }

    @Test
    void testMainPage() {
        var response = Unirest.get(BASE_URL + "/").asEmpty();
        assertEquals(HttpCode.OK.getStatus(), response.getStatus());
    }

    @Test
    void testGetAllUrls() {
        var response = Unirest.get(BASE_URL + "/urls").asEmpty();
        assertEquals(HttpCode.OK.getStatus(), response.getStatus());
    }

    @Test
    void testShowUrl() {
        var response = Unirest.get(BASE_URL + "/urls/{id}")
                .routeParam("id", "1")
                .asEmpty();

        var badResponse = Unirest.get(BASE_URL + "/urls/{id}")
                .routeParam("id", "badId")
                .asEmpty();

        assertEquals(HttpCode.OK.getStatus(), response.getStatus());
        assertEquals(HttpCode.NOT_FOUND.getStatus(), badResponse.getStatus());
    }

    @Test
    void testPostUrls() {
        var name = "https://minecraft.net";
        var postResponse = Unirest.post(BASE_URL + "/urls").field("url", name).asEmpty();
        var getResponse = Unirest.get(BASE_URL + "/urls").asString();
        var url = new QUrl().name.equalTo(name).findOneOrEmpty();

        assertTrue(getResponse.getBody().contains(name), String.format("Body contains '%s'", name));
        assertEquals(HttpCode.FOUND.getStatus(), postResponse.getStatus(), "Response status equals to FOUND");
        assertTrue(url.isPresent(), "Url added to database");
    }
}
