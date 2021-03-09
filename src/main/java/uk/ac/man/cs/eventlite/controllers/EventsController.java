package uk.ac.man.cs.eventlite.controllers;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;
	
	@Autowired
	private VenueService venueService;

	@GetMapping
	public String getAllEvents(Model model) {
		model.addAttribute("events", eventService.findAll());

		return "events/index";
	}
	
	
	@GetMapping("updateEvent/{id}")
	public String getEventById(@PathVariable("id") long id, Model model) {
		Optional<Event> event = eventService.findEventById(id);
		
		if (event.isEmpty()) {
			// If the event does not exist (null),
			// redirect to events page.
			
			return "redirect:/events";
		}
		
		model.addAttribute("event", event.get());
		
		// Add all available venues as model attributes so that
		// the user will be able to edit the venue attribute too.
		model.addAttribute("venues", venueService.findAll());
		
		// With all that information fetched, redirect
		// to updateEvent page and do the changes.
		return "events/updateEvent";
	}

	
	@PostMapping
	public String updateEvent(@RequestBody @Valid @ModelAttribute Event updatedEvent, BindingResult errors,
			Model model, RedirectAttributes redirectAttrs) {
		
		// Prints used for debugging only. Will be removed once everything works fine.
		System.out.println(updatedEvent.getId());
		System.out.println(updatedEvent.getName());
		System.out.println(updatedEvent.getTime());
		System.out.println(updatedEvent.getDate());
		System.out.println(updatedEvent.getVenue().getName());
		
		// The save method also acts as an update method, given that
		// the id of the updatedEvent is same as the id of event passed
		// to the model in the getEventById method above.
		eventService.save(updatedEvent);
		redirectAttrs.addFlashAttribute("ok_message", "Event updated successfuly.");
		
		return "redirect:/events";
	}
}
