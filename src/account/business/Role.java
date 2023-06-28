package account.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "roles_gen")
    @SequenceGenerator(name = "roles_gen", sequenceName = "roles_seq", allocationSize = 1)
    @JsonIgnore
    private Long id;

    private String name;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Role && Objects.equals(this.id, ((Role) obj).getId());
    }
}
