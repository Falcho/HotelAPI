package dat.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import dat.controllers.HotelController;
import dat.controllers.security.SecurityController;
import dat.enums.Roles;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes
{
    private static HotelController hotelController;
    private static SecurityController securityController;
    static ObjectMapper om = new ObjectMapper();

    public Routes(HotelController hotelController, SecurityController securityController)
    {
        Routes.hotelController = hotelController;
        Routes.securityController = securityController;
    }

    public static EndpointGroup getRoutes()
    {
        return () ->
        {
            path("hotel", () ->
            {
                get("/", hotelController::getAllHotels);
                get("/{id}", hotelController::getHotelById);
                post("/", hotelController::createHotel);
                put("/{id}", hotelController::updateHotel);
                delete("/{id}", hotelController::deleteHotel);
            });
            path("room", () ->
            {
                post("/{hotelid}", hotelController::addRoomToHotel);
                delete("/{id}", hotelController::deleteRoom);
            });
            path("hotel", () ->
            {
                get("/{hotelid}/rooms", hotelController::getRoomsForHotel);
            });
            path("/auth", () ->
            {
                post("/register", securityController.register());
                post("/login", securityController.login());
            });
            path("secured", () ->
            {
                get("demo", ctx ->
                        ctx.json(om.createObjectNode().put("demo", "secured")), Roles.USER);

            });
        };
    }

    public static void setHotelController(HotelController hController)
    {
        Routes.hotelController = hController;
    }

    public static void setSecurityController(SecurityController sController)
    {
        Routes.securityController = sController;
    }
}