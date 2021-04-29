package uk.ac.man.cs.eventlite.entities;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Helper class used to serialise an event entity
 * without the unwanted attributes.
 * 
 * @author jovan
 *
 */
public class APIEvent {
	private String name;
	private LocalTime time;
	private LocalDate date;
	
	public APIEvent(Event event) {
		name = event.getName();
		time = event.getTime();
		date = event.getDate();
	}
	
	public String getName() {
		return name;
	}
	
	public LocalTime getTime() {
		return time;
	}
	
	public LocalDate getDate() {
		return date;
	}
}
