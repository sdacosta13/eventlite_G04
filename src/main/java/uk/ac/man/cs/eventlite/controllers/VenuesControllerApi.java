package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.util.ArrayList;
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
	
	@GetMapping("/{venueId}/next3events")
	public CollectionModel<Event> getNextThreeEvents(@PathVariable long venueId) {
		return eventCollectionForVenue(eventService.nextEvents(venueService.findVenueById(venueId), 3), venueId);
	}
	
	private CollectionModel<Event> eventCollectionForVenue(Iterable<Event> events, long venueId) {
		Link selfLink = linkTo(methodOn(VenuesControllerApi.class).getNextThreeEvents(venueId)).withSelfRel();

		return CollectionModel.of(events, selfLink);
	}

	private CollectionModel<Venue> venueCollection(Iterable<Venue> venues) {
		Link selfLink = linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel();

		return CollectionModel.of(venues, selfLink);
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
