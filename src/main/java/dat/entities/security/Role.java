package dat.entities.security;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role
{
    @Id
    String name;

    @ManyToMany(mappedBy = "roles")
    Set<User> users = new HashSet<>();

    public Role(String name)
    {
        this.name = name;
    }

    public Role () {}

}
