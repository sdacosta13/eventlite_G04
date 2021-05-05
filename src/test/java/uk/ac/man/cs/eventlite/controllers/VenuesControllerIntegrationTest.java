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
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {
	
	private static Pattern CSRF = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
	private static String SESSION_KEY = "JSESSIONID";
	
	@LocalServerPort
	private int port;

	private WebTestClient client;
	
	private int numRows;

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
		numRows = countRowsInTable("venues");
	}

	@Test
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}
	
	@Test
	public void updateVenueNoAuthenticationTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "2");
		form.add("name", "My venue");
		form.add("address", "my address");
		form.add("postcode", "M13 9PL");
		form.add("capacity", "100");
		
		client.post().uri("/venues/updateVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/sign-in"));
		
		assertThat(numRows, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	public void updateVenueWithAuthenticationTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "2");
		form.add("name", "My venue");
		form.add("address", "my address");
		form.add("postcode", "M13 9PL");
		form.add("capacity", "100");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/venues/updateVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/venues"));
		
		assertThat(numRows, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	public void updateNonExistentVenueTest() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "111000");
		form.add("name", "My venue");
		form.add("address", "my address");
		form.add("postcode", "M13 9PL");
		form.add("capacity", "100");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/venues/updateVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectHeader().value("Location", endsWith("/venues"));
		
		assertThat(numRows, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	public void updateVenueNoName() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "2");
		form.add("name", "");
		form.add("address", "my address");
		form.add("postcode", "M13 9PL");
		form.add("capacity", "100");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/venues/updateVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	public void updateVenueBadName() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "2");
		form.add("name", "A very long string meant to exceed the limit .......  A very long string meant to exceed the limit .......  A very long string meant to exceed the limit ....... "
				+ "A very long string meant to exceed the limit .......  A very long string meant to exceed the limit .......  A very long string meant to exceed the limit ....... ");
		form.add("address", "my address");
		form.add("postcode", "M13 9PL");
		form.add("capacity", "100");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/venues/updateVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	public void updateVenueNoAddress() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "2");
		form.add("name", "My venue");
		form.add("address", "");
		form.add("postcode", "M13 9PL");
		form.add("capacity", "100");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/venues/updateVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	public void updateVenueBadAddress() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "2");
		form.add("name", "My venue");
		form.add("address", "A very long string meant to exceed the limit ....... A very long string meant to exceed the limit ....... A very long string meant to exceed the limit ......."
				+ "A very long string meant to exceed the limit ....... A very long string meant to exceed the limit ....... A very long string meant to exceed the limit .......");
		form.add("postcode", "M13 9PL");
		form.add("capacity", "100");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/venues/updateVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	public void updateVenueNoPostcode() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "2");
		form.add("name", "My venue");
		form.add("address", "my address");
		form.add("postcode", "");
		form.add("capacity", "100");
		
		// Session ID cookie holds login credentials.
		client.post().uri("/venues/updateVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	public void updateVenueBadCapacity() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "2");
		form.add("name", "My venue");
		form.add("address", "my address");
		form.add("postcode", "M13 9PL");
		form.add("capacity", "100.012");	// Floating point number
		
		// Session ID cookie holds login credentials.
		client.post().uri("/venues/updateVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	public void updateVenueBadCapacity2() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "2");
		form.add("name", "My venue");
		form.add("address", "my address");
		form.add("postcode", "M13 9PL");
		form.add("capacity", "-10");	// Negative number
		
		// Session ID cookie holds login credentials.
		client.post().uri("/venues/updateVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		
		assertThat(numRows, equalTo(countRowsInTable("venues")));
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
