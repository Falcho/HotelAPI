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

import java.io.FileReader;
import java.util.List;

public class ReadHotelsFromJson
{
    public static void main(String[] args)
    {
        try (FileReader fileReader = new FileReader("src/main/resources/hotels_with_rooms.json"))
        {
            // Read poems from json file
            ObjectMapper objectMapper = new ObjectMapper();
            HotelJsonWrapper hotelJsonWrapper = objectMapper.readValue(fileReader, HotelJsonWrapper.class);
            List<Hotel> hotels = hotelJsonWrapper.getHotels();
            EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
            GenericDAO genericDao = GenericDAO.getInstance(emf);

            for (Hotel hotel : hotels)
            {
                genericDao.create(hotel);
                for (Room room : hotel.getRooms())
                {
                    room.setHotel(hotel);
                    genericDao.create(room);

                }
                    //emf.close();
            }

        } catch (Exception e)
        {
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
