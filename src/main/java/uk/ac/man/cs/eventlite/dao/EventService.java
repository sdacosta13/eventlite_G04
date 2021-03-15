package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	public Iterable<Event> findAllByNameContaining(String infix);
	public Iterable<Event> findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc(String prefix, String suffix, String infix, String full);
	public Iterable<Event> findAllFromSearch(String infix);
	public Event findById(long id);

	public void save(Event entity);

	public void deleteById(long id);
	
	public Optional<Event> findEventById(long id);
}
