package uk.ac.man.cs.eventlite.controllers;

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
import org.springframework.test.web.reactive.server.WebTestClient;
import uk.ac.man.cs.eventlite.EventLite;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerApiIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {
	@LocalServerPort
	private int port;

	private WebTestClient client;

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/api").build();
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/venues").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$._links.self.href")
				.value(endsWith("/api/venues")).jsonPath("$._embedded.venues.length()").value(equalTo(3));
	}
	@Test
	public void getVenueTest() throws Exception {

		client.get().uri("/venues/3").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$.name").value(equalTo("NASA"))
				.jsonPath("$.capacity").value(equalTo(500))
				.jsonPath("$._links.length()").value(equalTo(4))
				.jsonPath("$._links.self.href").value(endsWith("/api/venues/3"))
				.jsonPath("$._links.venue.href").value(endsWith("/api/venues/3"))
				.jsonPath("$._links.events.href").value(endsWith("/api/venues/3/events"))
				.jsonPath("$._links.next3events.href").value(endsWith("/api/venues/3/next3events"));
	}
	@Test
	public void getVenueListTest() throws Exception{
		client.get().uri("/venues").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
		.contentType(MediaType.APPLICATION_JSON).expectBody()
		.jsonPath("$._links.self.href").value(endsWith("/api/venues"))
		.jsonPath("$._embedded.venues[0].id").value(equalTo(1))
		.jsonPath("$._embedded.venues[0].capacity").value(equalTo(100))
		.jsonPath("$._embedded.venues[0].address").value(equalTo("The University of Manchester\nOxford Rd\nManchester"))
		.jsonPath("$._embedded.venues[0].name").value(equalTo("Kilburn, G23"))
		.jsonPath("$._embedded.venues[0].postcode").value(equalTo("M13 9PL"))
		.jsonPath("$._embedded.venues[0].latitude").value(equalTo(53.470896))
		.jsonPath("$._embedded.venues[0].longitude").value(equalTo(-2.234521))
		;
	}
}
