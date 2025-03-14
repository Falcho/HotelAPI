import dat.config.HibernateConfig;
import dat.controllers.HotelController;
import dat.dtos.HotelDTO;

import dat.dtos.RoomDTO;
import dat.entities.Hotel;
import dat.entities.Room;
import dat.config.ApplicationConfig;
import dat.rest.Routes;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class TestClass
{
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private Hotel testHotel1, testHotel2;
    private Room testRoom2, testRoom1;

    @BeforeAll
    static void setupAll()
    {
        Routes.setHotelController(new HotelController(emf));
        ApplicationConfig.getInstance()
                .initiateServer()
                .setRoute(Routes.getRoutes())
                .handleException()
                .startServer(6060);

        RestAssured.baseURI = "http://localhost:6060/api";

    }
    @BeforeEach
    void setup()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Room").executeUpdate();
            em.createQuery("DELETE FROM Hotel").executeUpdate();
            testHotel1 = new Hotel();
            testHotel2 = new Hotel();
            testRoom1 = new Room();
            testRoom2 = new Room();
            testRoom1.setHotel(testHotel2);
            testRoom2.setHotel(testHotel2);
            em.persist(testHotel1);
            em.persist(testHotel2);
            testHotel2.addRoom(testRoom1);
            testHotel2.addRoom(testRoom2);
            em.persist(testRoom1);
            em.persist(testRoom2);
            em.merge(testHotel2);
            em.getTransaction().commit();
            System.out.println("Test data created");
        }
    }

    @AfterAll
    static void tearDown()
    {
        if (emf != null && emf.isOpen())
        {
            emf.close();
            System.out.println("EntityManagerFactory closed");
        }
        ApplicationConfig.getInstance().stopServer();
    }



    @Test
    void getAllHotelsTest()
    {
        int listSize = 2;

        given()
                .when()
                .get("/hotel")
                .then()
                .statusCode(200)
                .body("size()", equalTo(listSize));


    }

    @Test
    void getHotelByIdTest()
    {
        given()
                .pathParam("id", testHotel1.getId())
                .when()
                .get("/hotel/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(testHotel1.getId().intValue()));
    }

    @Test
    void createHotelTest ()
    {
        HotelDTO hotelDTO = new HotelDTO();
        List<RoomDTO> roomDTOs = new ArrayList<>();
        hotelDTO.setName("Test Hotel");
        hotelDTO.setAddress("Test Address");

        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setRoomNumber("1");
        roomDTO.setPrice(100);

        roomDTOs.add(roomDTO);
        hotelDTO.setRooms(roomDTOs);

        given()
                .body(hotelDTO)
                .when()
                .post("/hotel")
                .then()
                .statusCode(200)
                .body("id", is(notNullValue()))
                .body("name", equalTo(hotelDTO.getName()))
                .body("address", equalTo(hotelDTO.getAddress()))
                .body("rooms", hasSize(1));
    }

    @Test
    void deleteHotelTest()
    {
        given()
                .pathParam("id", testHotel1.getId())
                .when()
                .delete("/hotel/{id}")
                .then()
                .statusCode(204);
    }

    @Test
    void addRoomToHotelTest()
    {
        Room room = new Room();
        room.setRoomNumber("1");
        room.setPrice(100D);

        testHotel1.addRoom(room);

        given()
                .body(room)
                .when()
                .post("/room/"+testHotel1.getId())
                .then()
                .statusCode(201)
                .body("roomNumber", equalTo(room.getRoomNumber()))
                .body("price", equalTo(room.getPrice().floatValue()));
    }

    @Test
    void deleteRoomTest()
    {

        given()
                .when()
                .delete("/room/"+testRoom2.getId())
                .then()
                .statusCode(204);
    }

    @Test
    void getRoomsForHotelTest() {
        given()
                .when()
                .get("/hotel/"+testHotel2.getId()+"/rooms")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2));
    }

}
