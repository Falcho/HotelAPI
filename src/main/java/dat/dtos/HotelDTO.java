package dat.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dat.entities.Hotel;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelDTO
{
    private Long id;
    private String name;
    private String address;
    private List<RoomDTO> rooms;

    public HotelDTO(Hotel hotel)
    {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.address = hotel.getAddress();
        this.rooms = hotel.getRooms().stream()
                .map(RoomDTO::new)
                .collect(Collectors.toList());
    }
}
