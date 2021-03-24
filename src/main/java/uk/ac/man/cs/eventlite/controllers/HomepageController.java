package uk.ac.man.cs.eventlite.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;

@Controller
@RequestMapping(value = "/", produces = { MediaType.TEXT_HTML_VALUE })
public class HomepageController {
	
	@Autowired
	private EventService eventService;
	
	@GetMapping
	public String getAllVenues(Model model) {
		Iterable<Event> events = eventService.findAll();
		
		// Should probably replace with getting top 3 results rather than processing all of them here
		int count = 0;
		List<Event> upcomingEvents = new ArrayList<>();
		for (Event e : events) {
			if (e.isPast()) {
				continue;
			}
			else {
				upcomingEvents.add(e);
				count++;
			}
			if (count >= 3) {
				break;
			}
		}
		
		model.addAttribute("upcomingEvents", upcomingEvents);
		return "homepage/index";
	}

}
