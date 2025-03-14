package dat.entities.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements ISecurityUser
{
    @Id
    private String username;
    private String password;


    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "role_name"),
            inverseJoinColumns = @JoinColumn(name = "username"))
    Set<Role> roles = new HashSet<>();

    public User(String username, String password)
    {
        this.username = username;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public User()
    {
    }

    public void addRole(Role role)
    {
        roles.add(role);
        role.users.add(this);
    }

    @Override
    public void removeRole(String role)
    {
        //roles.removeIf(r -> r.name.equals(role));
        for(Role r : roles)
        {
            if(r.name.equals(role))
            {
                roles.remove(r);
                r.users.remove(this);
            }
        }

    }

    @Override
    public Set<String> getRolesAsStrings()
    {
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }

    @Override
    public boolean verifyPassword(String pw)
    {
        return BCrypt.checkpw(pw, this.password );

    }
}
