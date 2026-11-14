package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.bookings.Booking;
import hk.edu.polyu.comp.comp2021.crobs.model.core.CrobsSystem;
import hk.edu.polyu.comp.comp2021.crobs.model.rooms.Room;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;
import hk.edu.polyu.comp.comp2021.crobs.view.ParsedCommand;

import java.util.List;

/**
 * For handling room related commands (addRoom, modifyRoom, deleteRoom, listRoom, listAllRooms)
 */
public class RoomController {
    private final CrobsSystem system;

    private static final int ADD_ROOM_ARGS = 3;
    private static final int MODIFY_ROOM_ARGS = 3;
    private static final int TYPE_INDEX = 0;
    private static final int CAPACITY_INDEX = 1;
    private static final int FEATURES_INDEX = 2;
    private static final int ROOM_ID_INDEX = 0;
    private static final int FIELD_INDEX = 1;
    private static final int NEW_VALUE_INDEX = 2;

    /**
     * Constructor to initialise the controller and register its commands
     *
     * @param system the CrobsSystem object
     * @param registry the CommandController where the room commands get registered
     */
    public RoomController(CrobsSystem system, CommandController registry) {
        this.system = system;

        registry.register("addRoom", this::addRoom);
        registry.register("modifyRoom", this::modifyRoom);
        registry.register("deleteRoom", this::deleteRoom);
        registry.register("listRoom", this::listRoom);
        registry.register("listAllRooms", this::listAllRooms);
    }

    // private methods
    private String addRoom(ParsedCommand cmd) {
        cmd.requireArgs(ADD_ROOM_ARGS, "addRoom roomType capacity features");
        Room room = system.addRoom(cmd.arg(TYPE_INDEX), cmd.arg(CAPACITY_INDEX), cmd.arg(FEATURES_INDEX));
        return "Created room " + room.getId() + ".\n" + OutputFormatter.formatRoom(room);
    }
    private String modifyRoom(ParsedCommand cmd) {
        cmd.requireArgs(MODIFY_ROOM_ARGS, "modifyRoom roomID field newValue");
        Room room = system.modifyRoom(cmd.arg(ROOM_ID_INDEX), cmd.arg(FIELD_INDEX), cmd.arg(NEW_VALUE_INDEX));
        return "Updated room:\n" + OutputFormatter.formatRoom(room);
    }
    private String deleteRoom(ParsedCommand cmd) {
        cmd.requireArgs(1, "deleteRoom roomID");
        String id = cmd.arg(ROOM_ID_INDEX);
        List<Booking> cancelledBookings = system.deleteRoom(id);
        return "Deleted room " + id + "; cancelled " + cancelledBookings.size() + " future booking(s).";
    }
    private String listRoom(ParsedCommand cmd) {
        cmd.requireArgs(1, "listRoom roomID");
        Room room = system.getRoom(cmd.arg(ROOM_ID_INDEX));
        return OutputFormatter.formatRoom(room);
    }
    private String listAllRooms(ParsedCommand cmd) {
        cmd.requireArgs(0, "listAllRooms");
        return OutputFormatter.formatRooms(system.getAllRooms());
    }
}