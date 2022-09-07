package io.axoniq.labs.chat.commandmodel;

import io.axoniq.labs.chat.coreapi.CreateRoomCommand;
import io.axoniq.labs.chat.coreapi.JoinRoomCommand;
import io.axoniq.labs.chat.coreapi.LeaveRoomCommand;
import io.axoniq.labs.chat.coreapi.MessagePostedEvent;
import io.axoniq.labs.chat.coreapi.ParticipantJoinedRoomEvent;
import io.axoniq.labs.chat.coreapi.ParticipantLeftRoomEvent;
import io.axoniq.labs.chat.coreapi.PostMessageCommand;
import io.axoniq.labs.chat.coreapi.RoomCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Aggregate
public class ChatRoom {

    private static final Logger logger = LoggerFactory.getLogger(ChatRoom.class);

    @AggregateIdentifier
    private String roomId;
    private List<String> participants = new ArrayList<>();

    public ChatRoom() {}

    @CommandHandler
    public ChatRoom(CreateRoomCommand createRoomCommand) {
        AggregateLifecycle.apply(new RoomCreatedEvent(createRoomCommand.getRoomId(), createRoomCommand.getName()));
    }

    @EventSourcingHandler
    public void onRoomCreatedEvent(RoomCreatedEvent roomCreatedEvent) {
        this.roomId = roomCreatedEvent.getRoomId();
        logger.info("roomCreatedEvent: {}", roomCreatedEvent.getRoomId());
    }

    @CommandHandler
    public void handleJoinRoomCommand(JoinRoomCommand joinRoomCommand) {
        if (this.participants.contains(joinRoomCommand.getParticipant())) {
            return;
        }
        AggregateLifecycle.apply(new ParticipantJoinedRoomEvent(joinRoomCommand.getRoomId(), joinRoomCommand.getParticipant()));
    }

    @EventSourcingHandler
    public void onParticipantJoinedRoomEvent(ParticipantJoinedRoomEvent participantJoinedRoomEvent) {
        this.participants.add(participantJoinedRoomEvent.getParticipant());
    }

    @CommandHandler
    public void handleLeaveRoomCommand(LeaveRoomCommand leaveRoomCommand) {
        if (!this.participants.contains(leaveRoomCommand.getParticipant())) {
            return;
        }
        AggregateLifecycle.apply(new ParticipantLeftRoomEvent(leaveRoomCommand.getRoomId(), leaveRoomCommand.getParticipant()));
    }

    @EventSourcingHandler
    public void onParticipantLeftRoomEvent(ParticipantLeftRoomEvent participantLeftRoomEvent) {
        this.participants.remove(participantLeftRoomEvent.getParticipant());
    }

    @CommandHandler
    public void handlePostMessageCommand(PostMessageCommand postMessageCommand) {
        if (!this.participants.contains(postMessageCommand.getParticipant())) {
            throw new IllegalStateException("A participant may only post messages to rooms he/she has joined");
        }
        AggregateLifecycle.apply(new MessagePostedEvent(postMessageCommand.getRoomId(), postMessageCommand.getParticipant(), postMessageCommand.getMessage()));
    }
}
