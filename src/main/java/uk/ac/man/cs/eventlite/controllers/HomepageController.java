package uk.ac.man.cs.eventlite.controllers;

import java.util.ArrayList;
import java.util.Collections;
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
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/", produces = { MediaType.TEXT_HTML_VALUE })
public class HomepageController {
	
	@Autowired
	private EventService eventService;
	
	// Should rename to something better 
	private class VenueNum implements Comparable<VenueNum>{
		public Venue venue;
		public int count;
		
		public int compareTo(VenueNum other) {
			return Integer.compare(count, other.count);
		}
		
		public VenueNum(Venue venue, int count) {
			this.venue = venue;
			this.count = count;
		}
		
	}
	
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
		
		List<VenueNum> popularVenues = new ArrayList<>();
		for (Event e : events) {
			// Check if venue exists in list of venues
			boolean exists = false;
			for (VenueNum v : popularVenues) {
				// Increment number of events tied to venue if exists
				if (v.venue.equals(e.getVenue())) {
					exists = true;
					v.count++;
					break;
				}
			}
			
			// Else add new venue to list
			if (!exists) {
				popularVenues.add(new VenueNum(e.getVenue(), 1));
			}
		}
		Collections.sort(popularVenues, Collections.reverseOrder());
		
		// Trim to top 3 most populated venues
		if (popularVenues.size() > 3) {
			popularVenues = popularVenues.subList(0, 3);
		}
		
		model.addAttribute("upcomingEvents", upcomingEvents);
		model.addAttribute("popularVenues", popularVenues);
		return "homepage/index";
	}

}
