package dat;

import dat.config.HibernateConfig;
import dat.controllers.HotelController;
import dat.config.ApplicationConfig;
import dat.controllers.security.SecurityController;
import dat.services.Populator;
import dat.services.ReadHotelsFromJson;
import jakarta.persistence.EntityManagerFactory;
import dat.rest.Routes;


public class Main {
    final static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    final static HotelController hotelController = new HotelController(emf);
    final static SecurityController securityController = new SecurityController();

    public static void main(String[] args) {
        Routes.setHotelController(hotelController);
        Routes.setSecurityController(securityController);

        Populator.populate(args);

        ApplicationConfig
                .getInstance()
                .initiateServer()
                .checkSecurityRoles()
                .setRoute(Routes.getRoutes())
                .handleException()
                .startServer(7070);



    }
}