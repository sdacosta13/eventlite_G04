package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
import uk.ac.man.cs.eventlite.entities.Event;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerApiIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

	private WebTestClient client;

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/api").build();
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$._links.self.href")
				.value(endsWith("/api/events")).jsonPath("$._embedded.events.length()").value(equalTo(3));
	}
	
	@Test
	public void getEventTest() throws Exception {
		/**
		 * This test requires the event with id 5 to be
		 * present in the database.
		 */
		client.get().uri("/events/5").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$.date").value(equalTo(String.valueOf(LocalDate.of(2021, 5, 11))))
				.jsonPath("$.time").value(equalTo(String.valueOf(LocalTime.of(11, 00, 00).format(DateTimeFormatter.ofPattern("HH:mm:ss")))))
				.jsonPath("$.name").value(equalTo("COMP23412 Showcase, group H"))
				.jsonPath("$._links.length()").value(equalTo(3))
				.jsonPath("$._links.self.href").value(endsWith("/api/events/5"))
				.jsonPath("$._links.event.href").value(endsWith("/api/events/5"))
				.jsonPath("$._links.venue.href").value(endsWith("/api/events/5/venue"));
	}
	// The following tests assume the database has been reset
	@Test
	public void getEventsListTest() throws Exception{
		client.get().uri("/events").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
		.contentType(MediaType.APPLICATION_JSON).expectBody()
		.jsonPath("$._links.self.href").value(endsWith("/api/events"))
		.jsonPath("$._embedded.events[0].id").value(equalTo(6))
		.jsonPath("$._embedded.events[0].date").value(equalTo(String.valueOf(LocalDate.of(2021, 5, 10))))
		.jsonPath("$._embedded.events[0].time").value(equalTo(String.valueOf(LocalTime.of(16, 00, 00).format(DateTimeFormatter.ofPattern("HH:mm:ss")))))
		.jsonPath("$._embedded.events[0].name").value(equalTo("COMP23412 Showcase, group F"))
		.jsonPath("$._embedded.events[0].venue.id").value(equalTo(3));
	}
	
	
	
}
