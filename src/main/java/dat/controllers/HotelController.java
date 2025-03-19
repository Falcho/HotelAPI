package dat.controllers;

import dat.daos.GenericDAO;
import dat.dtos.ErrorMessage;
import dat.dtos.HotelDTO;
import dat.dtos.RoomDTO;
import dat.entities.Hotel;
import dat.entities.Room;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HotelController implements IController
{
    private final GenericDAO genericDAO;

    private static Logger logger = LoggerFactory.getLogger(HotelController.class);

    public HotelController(EntityManagerFactory emf)
    {
        genericDAO = GenericDAO.getInstance(emf);
    }

    public void getAllHotels(Context ctx)
    {
        try
        {
            logger.info("getAllHotels called");
            List<Hotel> hotels = genericDAO.findAll(Hotel.class);
            List<HotelDTO> hotelDTOs = hotels.stream()
                    .map(HotelDTO::new)
                    .collect(Collectors.toList());
            ctx.json(hotelDTOs);
        }
        catch (Exception e)
        {
            ErrorMessage error = new ErrorMessage("Error getting hotels" + e.getMessage());
            ctx.status(404).json(error);
        }
    }


    public void getHotelById(Context ctx)
    {
        try
        {
            long id = Long.parseLong(ctx.pathParam("id"));
            HotelDTO foundHotel = new HotelDTO(genericDAO.read(Hotel.class, id));
            ctx.json(foundHotel);
        }
        catch (Exception e)
        {
            ErrorMessage error = new ErrorMessage("No hotel with that id");
            ctx.status(404).json(error);
        }
    }

    public void createHotel(Context ctx) {
        try {
            HotelDTO hotelDTO = ctx.bodyAsClass(HotelDTO.class);
            Hotel hotel = new Hotel(hotelDTO);
            genericDAO.create(hotel);

            for (RoomDTO roomDTO : hotelDTO.getRooms()) {
                Room room = new Room(null, roomDTO.getRoomNumber(), roomDTO.getPrice(), hotel);
                genericDAO.create(room);
                roomDTO.setId(room.getId());
                roomDTO.setHotelId(hotel.getId());
            }

            hotelDTO.setId(hotel.getId());
            ctx.json(hotelDTO);
        } catch (Exception e) {
            ctx.status(400).json(Collections.singletonMap("message", "Error creating hotel"));
            e.printStackTrace();
        }
    }

    public void updateHotel(Context ctx)
    {
        try
        {
            int id = Integer.parseInt(ctx.pathParam("id"));
            HotelDTO incomingHotel = ctx.bodyAsClass(HotelDTO.class);
            Hotel hotel = new Hotel(incomingHotel);
            hotel.setId((long) id);
            Hotel updatedHotel = genericDAO.update(hotel);
            HotelDTO returnedHotel = new HotelDTO(updatedHotel);
            ctx.json(returnedHotel);
        }
        catch (Exception e)
        {
            ErrorMessage error = new ErrorMessage("Error updating hotel");
            ctx.status(400).json(error);
        }
    }

    public void deleteHotel(Context ctx)
    {
        try
        {
            long id = Long.parseLong(ctx.pathParam("id"));
            genericDAO.delete(Hotel.class, id);
            ctx.status(204);
        }
        catch (Exception e)
        {
            ErrorMessage error = new ErrorMessage("Error deleting hotel");
            ctx.status(400).json(error);
        }
    }

    public void addRoomToHotel(Context ctx) {
        try {
            long hotelId = Long.parseLong(ctx.pathParam("hotelid"));
            RoomDTO incomingRoomDTO = ctx.bodyAsClass(RoomDTO.class);

            // Find the existing hotel
            Hotel hotel = genericDAO.read(Hotel.class, hotelId);
            if (hotel == null) {
                logger.error("No hotel with that id" + hotelId);
                ctx.status(404).json(new ErrorMessage("Hotel not found"));
                return;
            }

            // Convert DTO to Room entity and set hotel reference
            Room room = new Room(incomingRoomDTO, hotel);

            // Save the room using the generic method
            genericDAO.create(room);

            // Optional: Update hotel's room list and merge
            hotel.getRooms().add(room);
            genericDAO.update(hotel);

            ctx.status(201).json(new RoomDTO(room));
        } catch (Exception e) {
            logger.error("Error adding room to hotel", e);
            ctx.status(400).json(new ErrorMessage("Error adding room to hotel"));
        }
    }


    public void deleteRoom(Context ctx) {
        try {
            long roomId = Long.parseLong(ctx.pathParam("id"));

            // Find the hotel and room
            Room room = genericDAO.read(Room.class, roomId);
            long hotelId = room.getHotel().getId();
            Hotel hotel = genericDAO.read(Hotel.class, hotelId);

            if (hotel == null) {
                ctx.status(404).json(new ErrorMessage("Hotel not found"));
                return;
            }
            if (room == null || !room.getHotel().getId().equals(hotelId)) {
                ctx.status(404).json(new ErrorMessage("Room not found in this hotel"));
                return;
            }

            // Remove the room from the hotel and delete
            hotel.getRooms().remove(room);
            genericDAO.update(hotel);
            genericDAO.delete(Room.class, roomId);

            ctx.status(204); // No content
        } catch (Exception e) {
            ctx.status(400).json(new ErrorMessage("Error deleting room"));
        }
    }


    public void getRoomsForHotel(Context ctx) {
        try {
            long hotelId = Long.parseLong(ctx.pathParam("hotelid"));

            // Find the hotel
            Hotel hotel = genericDAO.read(Hotel.class, hotelId);
            if (hotel == null) {
                ctx.status(404).json(new ErrorMessage("Hotel not found"));
                return;
            }

            // Convert Rooms to DTOs and return
            List<RoomDTO> roomDTOs = hotel.getRooms().stream()
                    .map(RoomDTO::new)
                    .toList();
            ctx.json(roomDTOs);
        } catch (Exception e) {
            ctx.status(400).json(new ErrorMessage("Error fetching rooms for hotel"));
        }
    }

}
