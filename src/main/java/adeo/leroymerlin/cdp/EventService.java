package adeo.leroymerlin.cdp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getEvents() {
        return eventRepository.findAllBy();
    }

    public boolean delete(Long id) {
        Optional<Event> eventInDB = eventRepository.findById(id);
        if (eventInDB.isPresent()) {
            eventRepository.deleteById(id);
        }
        Optional<Event> eventRemoved = eventRepository.findById(id);
        return eventRemoved.isEmpty();
    }

    public Event update(Long id, Event event) {
        Optional<Event> eventInDB = eventRepository.findById(id);
        if (eventInDB.isEmpty()) {
            throw new RuntimeException("Event with ID #" + id + " not found.");
        }
        if (!eventInDB.get().getId().equals(event.getId())) {
            throw new RuntimeException("Event in DB is not the same as the one to modify");
        }
        return eventRepository.save(event);
    }

    public List<Event> getFilteredEvents(String query) {
        List<Event> result = new ArrayList<>();

        if (query == null || query.isBlank()) {
            return result;
        }

        List<Event> events = eventRepository.findAllBy();
        // Filter the events list in pure JAVA here
        if (events == null || events.isEmpty()) {
            return result;
        }

        events.forEach(event -> {
            Set<Band> bands = event.getBands();
            if (bands != null && !bands.isEmpty()) {
                // Update Event title with Bands size
                event.setTitle(event.getTitle().concat(" [" + bands.size() + "]"));

                bands.forEach(band -> {
                    Set<Member> members = band.getMembers();
                    if (members != null && !members.isEmpty()) {
                        // Update Band title with Members size
                        band.setName(band.getName().concat(" [" + members.size() + "]"));

                        members.forEach(member -> {
                            if (member.getName().toLowerCase().contains(query.toLowerCase())) {
                                result.add(event);
                            }
                        });
                    }
                });
            }
        });
        // Pour éliminer les évènements duppliqués
        return result.stream().distinct().collect(Collectors.toList());
    }
}
