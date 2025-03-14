package dat.services;

import dat.daos.security.SecurityDAO;
import dat.enums.Roles;

public class Populator
{
    private static SecurityDAO securityDAO = new SecurityDAO();

    public static void populate(String[] args)
    {
    ReadHotelsFromJson.main(args);
        for (Roles role : Roles.values()) {
            securityDAO.createRole(role.name());
        }
    }


}
