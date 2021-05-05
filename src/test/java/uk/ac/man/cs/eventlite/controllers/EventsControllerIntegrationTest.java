package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.util.Iterator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;

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
	
	@Autowired
	private EventService eventService;

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
		numRows = countRowsInTable("events");
	}

	@Test
	public void testGetAllEvents() {
		Iterator<Event> iterator = eventService.findAll().iterator();
		assertTrue(iterator.hasNext());
		Event e = iterator.next();
		client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
		.expectBody(String.class).consumeWith(result -> {
            assertThat(result.getResponseBody(), containsString(e.getName()));
            assertThat(result.getResponseBody(), containsString(e.getDate().toString()));
            assertThat(result.getResponseBody(), containsString(e.getTime().toString()));
            assertThat(result.getResponseBody(), containsString(e.getVenue().getName()));
            assertThat(result.getResponseBody(), containsString(e.getDescription()));
            assertThat(result.getResponseBody(), containsString(String.valueOf(e.getVenue().getLongitude())));
            assertThat(result.getResponseBody(), containsString(String.valueOf(e.getVenue().getLatitude()))); 
		});
	}
	
	@Test
	public void testGetEvent() {
		Optional<Event> e = eventService.findEventById(4);
		assertTrue(e.isPresent());
		
		client.get().uri("/event/4").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
		.expectBody(String.class).consumeWith(result -> {
            assertThat(result.getResponseBody(), containsString(e.get().getName()));
            assertThat(result.getResponseBody(), containsString(e.get().getDate().toString()));
            assertThat(result.getResponseBody(), containsString(e.get().getTime().toString()));
            assertThat(result.getResponseBody(), containsString(e.get().getVenue().getName()));
            assertThat(result.getResponseBody(), containsString(e.get().getDescription()));
            assertThat(result.getResponseBody(), containsString(String.valueOf(e.get().getVenue().getLongitude())));
            assertThat(result.getResponseBody(), containsString(String.valueOf(e.get().getVenue().getLatitude())));
		});
	}
	
	@Test
	public void testGetNonExistingEvent() {
		client.get().uri("/event/123123123123121").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));
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
	public void addEventNoAuthenticationTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Test event");
		form.add("venue", "5");
		form.add("date", "2022-03-01");
		
		// Time and description are optional
		form.add("time", "");
		form.add("description", "");
		
		client.post().uri("/events/eventSubmit").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
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
		form.add("description", "Something here");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/events/updateEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));
		
		assertThat(numRows, equalTo(countRowsInTable("events")));
	}
	@Test
	public void addEventWithAuthenticationTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Test event");
		form.add("venue.id", "2");	// This venue (with id 2) must exist in the db
		form.add("date", "2022-03-01");
		
		// Time and description are optional
		form.add("time", "");
		form.add("description", "Something here");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/events/eventSubmit").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));
		
		assertThat(numRows+1, equalTo(countRowsInTable("events"))); //causes a successful add, so add 1
	}
	
	@Test
	public void updateNonExistingEvent() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "99999");  // Does not exist in the db
		form.add("name", "Event name");
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
		
		// The controller will just redirect to the events page
		// if non-existing event is requested to be updated
		
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
	public void addEventNoNameTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "");
		form.add("venue.id", "2");	// This venue (with id 2) must exist in the db
		form.add("date", "2022-03-01");
		
		// Time and description are optional
		form.add("time", "");
		form.add("description", "");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/events/eventSubmit").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
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
	@Test
	public void addEventBadNameTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... "
				+ "A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... ");
		form.add("venue.id", "2");	// This venue (with id 2) must exist in the db
		form.add("date", "2022-03-01");
		
		// Time and description are optional
		form.add("time", "");
		form.add("description", "");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/events/eventSubmit").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
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
	@Test
	public void addEventNoDateTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Testing...");
		form.add("venue.id", "2");	// This venue (with id 2) must exist in the db
		form.add("date", "");
		
		// Time and description are optional
		form.add("time", "");
		form.add("description", "");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/events/eventSubmit").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
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
	@Test
	public void addEventBadDateTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Testing...");
		form.add("venue.id", "2");	// This venue (with id 2) must exist in the db
		form.add("date", "2020-03-01"); // Date in the past
		
		// Time and description are optional
		form.add("time", "");
		form.add("description", "");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/events/eventSubmit").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void updateEventNoVenueTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "5");
		form.add("name", "Testing...");
		form.add("venue.id", "");	// Not specified
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
	@Test
	public void addEventNoVenueTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Testing...");
		form.add("venue.id", "");	// Not specified
		form.add("date", "2020-03-01"); // Date in the past
		
		// Time and description are optional
		form.add("time", "");
		form.add("description", "");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/events/eventSubmit").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void updateEventNonExistentVenueTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "5");
		form.add("name", "Testing...");
		form.add("venue.id", "1001001001");	// Non-existent venue
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
	@Test
	public void addEventNonExistentVenueTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Testing...");
		form.add("venue.id", "1001001001");	// Non-existent venue
		form.add("date", "2020-03-01"); // Date in the past
		
		// Time and description are optional
		form.add("time", "");
		form.add("description", "");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/events/eventSubmit").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void updateEventBadDescription() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "5");
		form.add("name", "New name..");
		form.add("venue.id", "2");	// This venue (with id 2) must exist in the db
		form.add("date", "2022-03-01");
		
		// Time is optional
		form.add("time", "");
		
		// Description is over 500 chars long
		form.add("description", "A very long string meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... "
				+ "A very long string meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... "
				+ "A very long string meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... ");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/events/updateEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("events")));
	}
	@Test
	public void addEventBadDescription() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "New name..");
		form.add("venue.id", "2");	// This venue (with id 2) must exist in the db
		form.add("date", "2022-03-01");
		
		// Time is optional
		form.add("time", "");
		
		// Description is over 500 chars long
		form.add("description", "A very long string meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... "
				+ "A very long string meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... "
				+ "A very long string meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... A very long name meant to exceed the limit ....... ");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/events/eventSubmit").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
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
