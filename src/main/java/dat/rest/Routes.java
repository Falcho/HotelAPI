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
                get("/", hotelController::getAllHotels, Roles.USER);
                get("/{id}", hotelController::getHotelById, Roles.USER);
                post("/", hotelController::createHotel, Roles.ADMIN);
                put("/{id}", hotelController::updateHotel, Roles.ADMIN);
                delete("/{id}", hotelController::deleteHotel, Roles.ADMIN);
            });
            path("room", () ->
            {
                post("/{hotelid}", hotelController::addRoomToHotel, Roles.ADMIN);
                delete("/{id}", hotelController::deleteRoom, Roles.ADMIN);
            });
            path("hotel", () ->
            {
                get("/{hotelid}/rooms", hotelController::getRoomsForHotel, Roles.USER);
            });
            path("/auth", () ->
            {
                post("/register", securityController.register(), Roles.ANYONE);
                post("/login", securityController.login(), Roles.ANYONE);
                get("/healthcheck", securityController::healthCheck, Roles.ANYONE);
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