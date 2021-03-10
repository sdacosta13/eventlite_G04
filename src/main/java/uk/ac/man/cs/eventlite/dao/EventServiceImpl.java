package uk.ac.man.cs.eventlite.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
	public void save(Event entity) {
		eventRepository.save(entity);
	}

	@Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}
}
