package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.DB;
import io.javalin.Javalin;
import io.javalin.http.HttpCode;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static hexlet.code.App.getApp;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class AppTest {
    private static final int PORT = 5002;
    private static final String BASE_URL = "http://localhost:" + PORT;
    private static Javalin app;
    private static MockWebServer mockServer;

    @BeforeAll
    static void beforeAll() {
        mockServer = new MockWebServer();
        app = getApp();
        app.start(PORT);

        // create sample data
        DB.getDefault().script().run("/dbscripts/seed.sql");
    }

    @AfterAll
    static void afterAll() throws IOException {
        app.stop();
        mockServer.shutdown();
    }

    @Test
    void testMainPage() {
        var response = Unirest.get(BASE_URL + "/").asString();

        assertTrue(response.getBody().contains("Анализатор страниц"));
        assertEquals(HttpCode.OK.getStatus(), response.getStatus(), "Response status is OK");
    }

    @Test
    void testGetAllUrls() {
        var response = Unirest.get(BASE_URL + "/urls").asString();
        var url1 = "https://google.com";
        var url2 = "https://facebook.com";
        var url3 = "https://my-site.com";

        assertTrue(response.getBody().contains(url1), String.format("Body contains '%s'", url1));
        assertTrue(response.getBody().contains(url2), String.format("Body contains '%s'", url2));
        assertTrue(response.getBody().contains(url3), String.format("Body contains '%s'", url3));
        assertEquals(HttpCode.OK.getStatus(), response.getStatus(), "Response status is OK");
    }

    @Test
    void testShowUrl() {
        var response = Unirest.get(BASE_URL + "/urls/{id}")
                .routeParam("id", "1")
                .asString();
        var badResponse = Unirest.get(BASE_URL + "/urls/{id}")
                .routeParam("id", "badId")
                .asEmpty();
        var url = "https://google.com";

        assertTrue(response.getBody().contains(url), String.format("Body contains '%s'", url));
        assertEquals(HttpCode.OK.getStatus(), response.getStatus(), "Response status is OK");
        assertEquals(HttpCode.NOT_FOUND.getStatus(), badResponse.getStatus(), "Response status is NOT_FOUND");
    }

    @Test
    void testPostUrls() {
        var name = "https://minecraft.net";
        var postResponse = Unirest.post(BASE_URL + "/urls").field("url", name).asEmpty();
        var getResponse = Unirest.get(BASE_URL + "/urls").asString();
        var url = new QUrl().name.equalTo(name).findOneOrEmpty();

        assertTrue(getResponse.getBody().contains(name), String.format("Body contains '%s'", name));
        assertEquals(HttpCode.FOUND.getStatus(), postResponse.getStatus(), "Response status is FOUND");
        assertTrue(url.isPresent(), "Url added to database");
    }

    @Test
    void testCheckUrl() throws IOException {
        var mockUrl = mockServer.url("some-site.com");
        var urlString = String.format("%s://%s:%s", mockUrl.scheme(), mockUrl.host(), mockUrl.port());

        MockResponse mockResponse = new MockResponse();
        Path filePath = Path.of("src/test/resources/sample-body.html");
        mockResponse.setBody(Files.readString(filePath));
        mockServer.enqueue(mockResponse);

        // add the URL to DB
        var response = Unirest
                .post(BASE_URL + "/urls")
                .field("url", urlString)
                .asString();
        assertEquals(response.getStatus(), HttpCode.FOUND.getStatus());

        var urlModel = new QUrl()
                .name.equalTo(urlString)
                .findOneOrEmpty()
                .orElse(null);
        assertNotNull(urlModel);

        // check the URL
        var response2 = Unirest
                .post(BASE_URL + "/urls/" + urlModel.getId() + "/checks")
                .asString();
        assertEquals(response2.getStatus(), HttpCode.FOUND.getStatus());

        var latestCheck = new QUrlCheck()
                .url.equalTo(urlModel)
                .createdAt.desc()
                .setMaxRows(1)
                .findOneOrEmpty()
                .orElse(null);
        assertNotNull(latestCheck);

        var response3 = Unirest.get(BASE_URL + "/urls/" + urlModel.getId()).asString();
        assertEquals(response3.getStatus(), HttpCode.OK.getStatus());
        var resBody = response3.getBody();
        assertTrue(resBody.contains(String.valueOf(latestCheck.getStatusCode())));
        assertTrue(resBody.contains(latestCheck.getUrl().getName()));
        assertTrue(resBody.contains(latestCheck.getH1()));
        assertTrue(resBody.contains(latestCheck.getDescription()));
        assertTrue(resBody.contains(latestCheck.getCreatedAt().toString()));
    }
}
