package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/venues", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE})
public class VenuesControllerApi {
	@Autowired
	private VenueService venueService;

	@Autowired
	private EventService eventService;

	@GetMapping
	public CollectionModel<Venue> getAllVenues() {
		return venueCollection(venueService.findAll());
	}

	private CollectionModel<Venue> venueCollection(Iterable<Venue> venues) {
		Link selfLink = linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel();

		return CollectionModel.of(venues, selfLink);
	}

	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<Venue>> getVenue(@PathVariable("id") long id) {
		Optional<Venue> venue = venueService.findVenueById(id);
		if (venue.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		LinkBuilder builder = linkTo(VenuesControllerApi.class).slash(id);
		Link selfLink = builder.withSelfRel();
		Link venueLink = builder.withRel("venue");
		Link eventsLink = builder.slash("events").withRel("events");
		Link next3EventsLink = builder.slash("next3events").withRel("next3events");

		EntityModel<Venue> model = EntityModel.of(venue.get())
				.add(selfLink, venueLink, eventsLink, next3EventsLink);

		return ResponseEntity.ok(model);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteVenue(@PathVariable("id") long id) {
		Optional<Venue> venue = venueService.findVenueById(id);
		
		if (venue.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Iterable<Event> events = eventService.findAllByVenue(venue.get());
		if (events.iterator().hasNext()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("\"Can not delete a venue that has events.\"");
		}

		venueService.deleteById(venue.get().getId());

		return ResponseEntity.noContent().build();
	}
}
