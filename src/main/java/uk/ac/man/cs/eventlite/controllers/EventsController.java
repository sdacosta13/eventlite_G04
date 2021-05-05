package uk.ac.man.cs.eventlite.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import twitter4j.Status;
import twitter4j.TwitterException;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.services.TwitterService;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;
	
	@Autowired
	private VenueService venueService;
	
	@Autowired
	private TwitterService twitterService;

	@GetMapping
	public String getAllEvents(@RequestParam(value = "name", required = false) String name, Model model) {
		Iterable<Event> result;

		if (name == null) {
			result = eventService.findAll();
		} else {
			result = eventService.findAllByNameContaining(name);
		}

		List<Event> pastEvents = new ArrayList<>();
		List<Event> futureEvents = new ArrayList<>();
		
		for (Event e : result) {
			if (e.isPast()) 
				pastEvents.add(e);
			else
				futureEvents.add(e);
		}

		model.addAttribute("pastEvents", pastEvents);
		model.addAttribute("futureEvents", futureEvents);
		
		List<Status> mostRecentTweets;
		
		try {
			mostRecentTweets = twitterService.getTimeLine(5);
		} catch (TwitterException e) {
			mostRecentTweets = Collections.emptyList();
		}
		
		model.addAttribute("mostRecentTweets", mostRecentTweets);

		return "events/index";
	}

	@GetMapping(value="/addEvent")
	public String getEventAdder(Model model) {
		model.addAttribute("venues", venueService.findAll());
		model.addAttribute("event", new Event());
		return "events/addEvent";
	}
	
	@PostMapping(value="/eventSubmit")
	public String addEvent(@RequestBody @Valid @ModelAttribute Event event, BindingResult errors,
			Model model, RedirectAttributes redirectAttrs) {
		if (errors.hasErrors()) {
			model.addAttribute("event", event);
			model.addAttribute("venues", venueService.findAll());
			return "events/addEvent";
		}
		eventService.save(event);
		return "redirect:/events";
	}
	@DeleteMapping("/{eventId}")
	public String deleteEvent(@PathVariable long eventId, RedirectAttributes redirectAttrs) {
		eventService.deleteById(eventId);
		redirectAttrs.addFlashAttribute("ok_message", "Event deleted.");
		return "redirect:/events";
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
	
	@PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, value="updateEvent")
	public String updateEvent(@RequestBody @Valid @ModelAttribute Event event, BindingResult errors,
			Model model, RedirectAttributes redirectAttrs) {
		
		if (errors.hasErrors()) {
			model.addAttribute("event", event);
			model.addAttribute("venues", venueService.findAll());
			return "events/updateEvent";
		}
		
		// Check that the event we are trying to update exists
		if (eventService.findById(event.getId()) == null) {
			redirectAttrs.addFlashAttribute("error_message", "Event does not exist.");
			return "redirect:/events";
		}
		
		// Check that the venue specified exsits
		if (venueService.findById(event.getVenue().getId()) == null) {
			redirectAttrs.addFlashAttribute("error_message", "Venue does not exist.");
			return "redirect:/events/updateEvent";
		}
		
		// The save method also acts as an update method, given that
		// the id of the updatedEvent is same as the id of event passed
		// to the model in the getEventById method above.
		eventService.save(event);
		redirectAttrs.addFlashAttribute("ok_message", "Event updated successfuly.");
		
		return "redirect:/events";
	}
}
