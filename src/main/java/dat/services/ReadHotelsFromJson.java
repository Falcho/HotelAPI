package dat.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dat.config.HibernateConfig;
import dat.daos.GenericDAO;
import dat.entities.Hotel;
import dat.entities.Room;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ReadHotelsFromJson
{
    public static void main(String[] args)
    {
        try (Reader reader = new InputStreamReader(
                ReadHotelsFromJson.class.getClassLoader().getResourceAsStream("hotels_with_rooms.json"),
                StandardCharsets.UTF_8)) {
            if (reader == null) {
                throw new FileNotFoundException("Resource not found: hotels_with_rooms.json");
            }

            // Read hotels from json file
            ObjectMapper objectMapper = new ObjectMapper();
            HotelJsonWrapper hotelJsonWrapper = objectMapper.readValue(reader, HotelJsonWrapper.class);
            List<Hotel> hotels = hotelJsonWrapper.getHotels();
            EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
            GenericDAO genericDao = GenericDAO.getInstance(emf);

            for (Hotel hotel : hotels) {
                genericDao.create(hotel);
                for (Room room : hotel.getRooms()) {
                    room.setHotel(hotel);
                    genericDao.create(room);
                }
            }
            // emf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class HotelJsonWrapper
    {
        private List<Hotel> hotels;
    }
}
