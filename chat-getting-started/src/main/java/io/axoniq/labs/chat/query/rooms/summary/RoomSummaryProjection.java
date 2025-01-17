package io.axoniq.labs.chat.query.rooms.summary;

import io.axoniq.labs.chat.coreapi.AllRoomsQuery;
import io.axoniq.labs.chat.coreapi.ParticipantJoinedRoomEvent;
import io.axoniq.labs.chat.coreapi.ParticipantLeftRoomEvent;
import io.axoniq.labs.chat.coreapi.RoomCreatedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RoomSummaryProjection {

    private final RoomSummaryRepository roomSummaryRepository;

    public RoomSummaryProjection(RoomSummaryRepository roomSummaryRepository) {
        this.roomSummaryRepository = roomSummaryRepository;
    }

    @QueryHandler
    public List<RoomSummary> handle(AllRoomsQuery query) {
        return roomSummaryRepository.findAll();
    }

    @EventHandler
    public void on(RoomCreatedEvent event) {
        roomSummaryRepository.save(new RoomSummary(event.getRoomId(), event.getName()));
    }

    @EventHandler
    public void on(ParticipantJoinedRoomEvent event) {
        Optional<RoomSummary> s = roomSummaryRepository.findById(event.getRoomId());
        if (s.isPresent()) {
            RoomSummary roomSummary = s.get();
            roomSummary.addParticipant();
        }
    }

    @EventHandler
    public void on(ParticipantLeftRoomEvent event) {
        Optional<RoomSummary> s = roomSummaryRepository.findById(event.getRoomId());
        if (s.isPresent()) {
            RoomSummary roomSummary = s.get();
            roomSummary.removeParticipant();
        }
    }
}
