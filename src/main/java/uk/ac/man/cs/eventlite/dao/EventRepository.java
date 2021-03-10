package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import uk.ac.man.cs.eventlite.entities.Event;

public interface EventRepository extends CrudRepository<Event, Long> {
	public Iterable<Event> findAllByOrderByDateAsc();
	
	public Iterable<Event> findAllByNameContaining(String infix);
	
	public Event findById(long id);
	
	public Optional<Event> findEventById(long id);
}
