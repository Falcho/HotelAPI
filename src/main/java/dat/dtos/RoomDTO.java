package dat.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dat.entities.Room;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomDTO
{
    private Long id;
    private Long hotelId;
    private String roomNumber;
    private Double price;


    public RoomDTO(Room room)
    {
        this.id = room.getId();
        this.roomNumber = room.getRoomNumber();
        this.price = room.getPrice();
        this.hotelId = room.getHotel().getId();
    }
}
