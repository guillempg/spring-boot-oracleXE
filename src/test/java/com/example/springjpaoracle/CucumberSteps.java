package com.example.springjpaoracle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ConfigurableApplicationContext;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class CucumberSteps
{
    private final ConfigurableApplicationContext context;
    private final Authenticator authenticator = new Authenticator()
    {
        @Override
        public PasswordAuthentication requestPasswordAuthenticationInstance(final String host, final InetAddress addr, final int port,
                                                                            final String protocol, final String prompt,
                                                                            final String scheme, final URL url, final RequestorType reqType)
        {
            return super.requestPasswordAuthenticationInstance(host, addr, port, protocol, prompt, scheme, url, reqType);
        }
    };
    private final HttpClient client = HttpClient.newBuilder()
    .version(HttpClient.Version.HTTP_2)
            .authenticator(this.authenticator)
            .build();;

    public CucumberSteps(final ConfigurableApplicationContext context, final Authenticator authenticator)
    {
        this.context = context;
    }

    @Given("the app is running and connected to database")
    public void connectToDatabaseAndStartApp() {

        assertTrue(context.isRunning());
    }

    @When("we successfully create student with details:")
    public void we_create_student_with_details(io.cucumber.datatable.DataTable dataTable) throws JSONException
    {

        final var CREATE_STUDENT = URI.create("http://localhost:8080/student/create");

        final JSONObject json = new JSONObject();
        json.put("name","Wile E. Coyote");

        final JSONArray coursesJson = new JSONArray();

        for (Map<String,String> entry : dataTable.asMaps())
        {
            final List<String> courses = Arrays.asList(entry.get("courses").split(","));
            int idx = 0;
            for (String course : courses)
            {
                coursesJson.put(idx++, course);
            }
        }
        json.put("courses",coursesJson);

        final var request = HttpRequest.newBuilder()
            .uri(CREATE_STUDENT)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
            .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .join();

    }
}
