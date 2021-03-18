package uk.ac.man.cs.eventlite.dao;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Event;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
// @Disabled
public class EventServiceTest extends AbstractTransactionalJUnit4SpringContextTests {
	
	@InjectMocks
	private EventServiceImpl eventService;
	
	@Mock
	private EventRepository eventRepository;
	
	@Mock 
	private Event e1 = mock(Event.class, Mockito.RETURNS_DEEP_STUBS);
	@Mock 
	private Event e2 = mock(Event.class, Mockito.RETURNS_DEEP_STUBS);
	@Mock 
	private Event e3 = mock(Event.class, Mockito.RETURNS_DEEP_STUBS);
	@Mock 
	private Event e4 = mock(Event.class, Mockito.RETURNS_DEEP_STUBS);
	@Mock 
	private Event e5 = mock(Event.class, Mockito.RETURNS_DEEP_STUBS);
	@Mock 
	private Event e6 = mock(Event.class, Mockito.RETURNS_DEEP_STUBS);
	@Mock 
	private Event e7 = mock(Event.class, Mockito.RETURNS_DEEP_STUBS);
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(e1.getName()).thenReturn("Event a b");
		when(e2.getName()).thenReturn("Event b");
		when(e3.getName()).thenReturn("Event c a");
		when(e4.getName()).thenReturn("Event d a");
		when(e5.getName()).thenReturn("Event e");
		when(e6.getName()).thenReturn("Event f d a");
		when(e7.getName()).thenReturn("Event g");
		
		when(eventRepository.findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc(anyString(),anyString(),anyString(),anyString())).thenReturn(setupEventsArray());
		when(eventRepository.findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc("a %", "% a", "% a %", "a")).thenReturn(setupEventsArrayA());
		when(eventRepository.findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc("d %", "% d", "% d %", "d")).thenReturn(setupEventsArrayD());
	}
	
	private Iterable<Event> setupEventsArrayA() {
		ArrayList<Event> evAr = new ArrayList<Event>();
		evAr.add(e1);
		evAr.add(e3);
		evAr.add(e4);
		evAr.add(e6);
		return (Iterable<Event>) evAr;
	}
	
	private Iterable<Event> setupEventsArrayD() {
		ArrayList<Event> evAr = new ArrayList<Event>();
		evAr.add(e4);
		evAr.add(e6);
		return (Iterable<Event>) evAr;
	}
	
	private Iterable<Event> setupEventsArray(){			
		ArrayList<Event> evAr = new ArrayList<Event>();
		evAr.add(e1);
		evAr.add(e2);
		evAr.add(e3);
		evAr.add(e4);
		evAr.add(e5);
		evAr.add(e6);
		evAr.add(e7);
		
		return (Iterable<Event>) evAr;
	}
	
	@Test
	public void testSearchByMultipleKeywordsUsingRegex1() {
		String testName = "Event a";
		Iterable<Event> results = eventService.findAllMultipleKeywordsUsingRegex(testName);
		verify(eventRepository, times(1)).findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc(anyString(), anyString(), anyString(), anyString());
		
		Iterator<Event> iterator = results.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(e1, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(e3, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(e4, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(e6, iterator.next());
		assertFalse(iterator.hasNext());
		
	}
	
	@Test
	public void testSearchByMultipleKeywordsUsingRegex2() {
		String testName = "Event d";
		Iterable<Event> results = eventService.findAllMultipleKeywordsUsingRegex(testName);
		verify(eventRepository, times(1)).findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc(anyString(), anyString(), anyString(), anyString());
		
		
		Iterator<Event> iterator = results.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(e4, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(e6, iterator.next());
		assertFalse(iterator.hasNext());
	}
	
	@Test
	public void testSearchByMultipleKeywords1() {
		String testName = "Event a";
		Iterable<Event> results = eventService.findAllMultipleKeywords(testName);
		verify(eventRepository, times(2)).findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc(anyString(), anyString(), anyString(), anyString());
		
		Iterator<Event> iterator = results.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(e1, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(e3, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(e4, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(e6, iterator.next());
		assertFalse(iterator.hasNext());
		
	}
	
	@Test
	public void testSearchByMultipleKeywords2() {

		
		String testName = "Event a d";
		Iterable<Event> results = eventService.findAllMultipleKeywords(testName);
		verify(eventRepository, times(3)).findAllContainingAlternativeIgnoreCaseOrderByNameAscDateAsc(anyString(), anyString(), anyString(), anyString());
		
		
		Iterator<Event> iterator = results.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(e4, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(e6, iterator.next());
		assertFalse(iterator.hasNext());
	}
	// This class is here as a starter for testing any custom methods within the
	// EventService. Note: It is currently @Disabled!
}
