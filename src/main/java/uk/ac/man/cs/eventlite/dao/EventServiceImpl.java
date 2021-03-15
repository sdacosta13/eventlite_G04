package uk.ac.man.cs.eventlite.dao;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import uk.ac.man.cs.eventlite.entities.Event;

@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	private final static String DATA = "data/events.json";

	@Autowired
	private EventRepository eventRepository;

	@Override
	public long count() {
		return eventRepository.count();
	}

	@Override
	public Iterable<Event> findAll() {
		return eventRepository.findAllByOrderByDateAsc();
	}
	
	@Override
	public Iterable<Event> findAllByNameContaining(String infix) {
		return eventRepository.findAllByNameContaining(infix);
	}
	
	@Override
	public Iterable<Event> findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc(String prefix, String suffix, String infix, String full) {
		return eventRepository.findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc(prefix, suffix, infix, full);
	}
	
	@Override
	public Iterable<Event> findAllFromSearch(String name) {
		return findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc(name + " %", "% " + name, "% " + name + " %", name);
	}
	
	public Event findById(long id) {
		return eventRepository.findById(id);
	}

	@Override
	public void save(Event entity) {
		eventRepository.save(entity);
	}

	@Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}
	
	@Override
	public Optional<Event> findEventById(long id) {
		return eventRepository.findEventById(id);
	}
}
