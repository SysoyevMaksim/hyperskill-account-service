package account.persistence;

import account.business.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);

    @Query(value = "SELECT COUNT(*) FROM user_role INNER JOIN roles on user_role.role_id = roles.id WHERE name = ?1", nativeQuery = true)
    Long getNumberOfUse(String name);
}
