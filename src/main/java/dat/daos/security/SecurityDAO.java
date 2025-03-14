package dat.daos.security;

import dat.config.HibernateConfig;
import dat.enums.Roles;
import dat.exceptions.ValidationException;
import dat.entities.security.Role;
import dat.entities.security.User;
import dk.bugelhartmann.UserDTO;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class SecurityDAO
{
    EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static SecurityDAO instance;

    public static SecurityDAO getInstance(EntityManagerFactory emf)
    {
        if (instance == null)
        {
            instance = new SecurityDAO();
        }
        return instance;
    }

    public User CreateUser(User user)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            Set<Role> roles = new HashSet<>();
            if(user.getRoles().isEmpty()){
                Role userRole = em.find(Role.class, Roles.USER.name());
                if(userRole == null){
                    userRole = new Role(Roles.USER.name());
                    em.persist(userRole);
                }
                user.addRole(userRole);
            }
            user.getRoles().forEach(role ->
            {
                Role foundRole = em.find(Role.class, role.getName());
                if (foundRole == null)
                {
                    throw new EntityNotFoundException("No role found with that id");
                } else
                {
                    roles.add(foundRole);
                }
            });
            user.setRoles(roles);
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return user;
        } catch (EntityExistsException e){
            throw new EntityExistsException();
        }
    }

    public void createRole(String roleName)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            if (em.find(Role.class, roleName.toUpperCase()) == null)
            {
                em.getTransaction().begin();
                Role newRole = new Role(roleName.toUpperCase());
                em.persist(newRole);
                em.getTransaction().commit();
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public UserDTO getVerifiedUser(String username, String password) throws ValidationException
    {
        try (EntityManager em = emf.createEntityManager()) {
            User user = em.find(User.class, username);
            if (user == null)
                throw new EntityNotFoundException("No user found with username: " + username); //RuntimeException
            user.getRoles().size(); // force roles to be fetched from db
            if (!user.verifyPassword(password))
                throw new ValidationException("Wrong password");
            return new UserDTO(user.getUsername(), user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()));
        }
    }
}
