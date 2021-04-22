package uk.ac.man.cs.eventlite.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import twitter4j.Status;
import twitter4j.TwitterException;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.services.TwitterService;

@Controller
@RequestMapping(value = "event/{eventId}", produces = { MediaType.TEXT_HTML_VALUE })
public class EventPageController {
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private TwitterService twitterService;
	
	@GetMapping
	public String getEventInfo(Model model, @PathVariable Long eventId) {
		model.addAttribute("event", eventService.findById(eventId));
		return "events/info-page";
	}
	
	@PostMapping(value="/tweetSubmit")
	public String tweetSubmit(@RequestBody @Valid @ModelAttribute("message") String message, RedirectAttributes redirectAttrs, HttpServletRequest request) {
		if (twitterService.postTweet(message)) {
			redirectAttrs.addFlashAttribute("ok_message", String.format("Your tweet: '%s' was posted", message));
		} else {
			redirectAttrs.addFlashAttribute("error_message", "Twitter is not available.");
		}	
		return "redirect:" + request.getHeader("Referer");	
	}

}
