package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventRepository extends CrudRepository<Event, Long> {
	public Iterable<Event> findAllByOrderByDateAsc();
	
	public Iterable<Event> findAllByNameContaining(String infix);
	public Iterable<Event> findAllByNameContainingIgnoreCaseOrderByDateAscTimeAsc(String infix);
	public Iterable<Event> findAllByNameContainingIgnoreCaseOrderByNameAscDateAsc(String infix);
	
	public Iterable<Event> findAllByVenue(Venue venue);

	@Query("SELECT e FROM Event e where UPPER(e.name) like UPPER(?1) or UPPER(e.name) like UPPER(?2) or UPPER(e.name) like UPPER(?3) or UPPER(e.name) like UPPER(?4) ORDER BY e.name ASC, e.date ASC")
	public Iterable<Event> findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc(String prefix, String suffix, String infix, String full);
	
	@Query("SELECT e FROM Event e where e.venue = ?1")
	public Iterable<Event> findEventsAtVenue(Venue venue);
	
	public Event findById(long id);
	
	public Optional<Event> findEventById(long id);
}
