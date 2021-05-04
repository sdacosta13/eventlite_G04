package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {
	
	private static Pattern CSRF = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
	private static String SESSION_KEY = "JSESSIONID";

	@LocalServerPort
	private int port;
	
	private int numRows;

	private WebTestClient client;

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
		numRows = countRowsInTable("events");
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}
	
	@Test
	public void updateEventNoAuthenticationTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "5");
		form.add("name", "Test event");
		form.add("venue", "5");
		form.add("date", "2022-03-01");
		
		// Time and description are optional
		form.add("time", "");
		form.add("description", "");
		
		client.post().uri("/events/updateEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/sign-in"));
		
		assertThat(numRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void updateEventWithAuthenticationTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "5");
		form.add("name", "Test event");
		form.add("venue.id", "2");	// This venue (with id 2) must exist in the db
		form.add("date", "2022-03-01");
		
		// Time and description are optional
		form.add("time", "");
		form.add("description", "");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/events/updateEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));
		
		assertThat(numRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void updateEventNoNameTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "5");
		form.add("name", "");
		form.add("venue.id", "2");	// This venue (with id 2) must exist in the db
		form.add("date", "2022-03-01");
		
		// Time and description are optional
		form.add("time", "");
		form.add("description", "");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/events/updateEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void updateEventBadNameTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "5");
		form.add("name", "A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... "
				+ "A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... ");
		form.add("venue.id", "2");	// This venue (with id 2) must exist in the db
		form.add("date", "2022-03-01");
		
		// Time and description are optional
		form.add("time", "");
		form.add("description", "");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/events/updateEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("events")));
	}
	
	public void updateEventNoDateTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "5");
		form.add("name", "Testing...");
		form.add("venue.id", "2");	// This venue (with id 2) must exist in the db
		form.add("date", "");
		
		// Time and description are optional
		form.add("time", "");
		form.add("description", "");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/events/updateEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("events")));
	}
	
	public void updateEventBadDateTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "5");
		form.add("name", "Testing...");
		form.add("venue.id", "2");	// This venue (with id 2) must exist in the db
		form.add("date", "2020-03-01"); // Date in the past
		
		// Time and description are optional
		form.add("time", "");
		form.add("description", "");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/events/updateEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("events")));
	}
	
	private String[] login() {
		String[] tokens = new String[2];

		EntityExchangeResult<String> result = client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get()
				.uri("/").accept(MediaType.TEXT_HTML).exchange().expectBody(String.class).returnResult();
		tokens[0] = getCsrfToken(result.getResponseBody());
		tokens[1] = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		return tokens;
	}
	
	private String getCsrfToken(String body) {
		Matcher matcher = CSRF.matcher(body);
		assertThat(matcher.matches(), equalTo(true));

		return matcher.group(1);
	}
}
