package io.axoniq.labs.chat.query.rooms.participants;

import io.axoniq.labs.chat.coreapi.ParticipantJoinedRoomEvent;
import io.axoniq.labs.chat.coreapi.ParticipantLeftRoomEvent;
import io.axoniq.labs.chat.coreapi.RoomParticipantsQuery;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoomParticipantsProjection {

    private final RoomParticipantsRepository repository;

    public RoomParticipantsProjection(RoomParticipantsRepository repository) {
        this.repository = repository;
    }

    @QueryHandler
    public List<String> handle(RoomParticipantsQuery query) {
        return repository.findRoomParticipantsByRoomId(query.getRoomId())
                .stream()
                .map(RoomParticipant::getParticipant).sorted().collect(Collectors.toList());
    }

    @EventHandler
    public void on(ParticipantJoinedRoomEvent participantJoinedRoomEvent) {
        repository.save(new RoomParticipant(
                participantJoinedRoomEvent.getRoomId(),
                participantJoinedRoomEvent.getParticipant()
        ));
    }

    @EventHandler
    public void on(ParticipantLeftRoomEvent participantLeftRoomEvent) {
        repository.deleteByParticipantAndRoomId(
                participantLeftRoomEvent.getParticipant(),
                participantLeftRoomEvent.getRoomId()
        );
    }
}
