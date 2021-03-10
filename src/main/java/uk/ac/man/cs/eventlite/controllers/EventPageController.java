package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.ac.man.cs.eventlite.dao.EventService;

@Controller
@RequestMapping(value = "events/{eventId}", produces = { MediaType.TEXT_HTML_VALUE })
public class EventPageController {
	
	@Autowired
	private EventService eventService;
	
	@GetMapping
	public String getEventInfo(Model model, @PathVariable Long eventId) {
		model.addAttribute("event", eventService.findById(eventId));
		return "events/info-page";
	}

}
