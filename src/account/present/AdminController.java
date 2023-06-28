package account.present;

import account.business.*;
import account.persistence.LogRepository;
import account.persistence.RoleRepository;
import account.persistence.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@RestController
public class AdminController {
    private final UserRepository userRepo;

    private final LogRepository logRepository;

    private final RoleRepository roleRepo;

    public AdminController(UserRepository userRepo, LogRepository logRepository, RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.logRepository = logRepository;
        this.roleRepo = roleRepo;
    }

    @GetMapping("/api/admin/user/")
    public List<User> getUsers() {
        List<User> list = userRepo.findAll();
        for (User user : list) {
            user.setStringRoles(new LinkedList<>());
            for (Role role : user.getRoles()) {
                user.getStringRoles().add(role.getName());
            }
        }
        return list;
    }

    @DeleteMapping("/api/admin/user/{email}")
    public UserStatus2 deleteUser(@AuthenticationPrincipal UserDetails details, @PathVariable String email) {
        email = email.toLowerCase();
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
        if (user.getRoles().contains(roleRepo.findByName("ROLE_ADMINISTRATOR"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        }
        userRepo.deleteById(user.getId());
        logRepository.save(new Log(null, LocalDateTime.now().toString(), "DELETE_USER",
                details.getUsername(), user.getEmail(), "/api/admin/user"));
        return new UserStatus2(email, "Deleted successfully!");
    }

    @PutMapping("/api/admin/user/role")
    public User changeRole(@AuthenticationPrincipal UserDetails details, @RequestBody ChangeRole changeRole) {
        changeRole.setUser(changeRole.getUser().toLowerCase());
        User user = userRepo.findByEmail(changeRole.getUser());
        Role role = roleRepo.findByName("ROLE_" + changeRole.getRole());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
        }
        if (Objects.equals(changeRole.getOperation(), "GRANT")) {
            if ((user.getRoles().contains(roleRepo.findByName("ROLE_ADMINISTRATOR"))
                    && !Objects.equals(role.getName(), "ROLE_ADMINISTRATOR"))
                    || (!user.getRoles().contains(roleRepo.findByName("ROLE_ADMINISTRATOR"))
                    && Objects.equals(role.getName(), "ROLE_ADMINISTRATOR"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user cannot combine administrative and business roles!");
            }
            user.getRoles().add(role);
            logRepository.save(new Log(null, LocalDateTime.now().toString(), "GRANT_ROLE",
                    details.getUsername(), "Grant role " + changeRole.getRole() + " to " + user.getEmail(), "/api/admin/user/role"));
        } else {
            if (Objects.equals(role.getName(), "ROLE_ADMINISTRATOR")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
            }
            if (!user.getRoles().contains(role)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
            }
            if (user.getRoles().size() == 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
            }
            user.getRoles().remove(role);
            logRepository.save(new Log(null, LocalDateTime.now().toString(), "REMOVE_ROLE",
                    details.getUsername(), "Remove role ACCOUNTANT from " + user.getEmail(), "/api/admin/user/role"));
        }
        user.getRoles().sort((a, b) -> CharSequence.compare(a.getName(), b.getName()));
        userRepo.save(user);
        user.setStringRoles(new LinkedList<>());
        for (Role r : user.getRoles()) {
            user.getStringRoles().add(r.getName());
        }
        return user;
    }

    @PutMapping("/api/admin/user/access")
    public OneLineStatus changeAccess(@AuthenticationPrincipal UserDetails details, @RequestBody ChangeAccess changeAccess) {
        changeAccess.setUser(changeAccess.getUser().toLowerCase());
        User user = userRepo.findByEmail(changeAccess.getUser());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
        if (Objects.equals(changeAccess.getOperation(), "LOCK")) {
            if (user.getRoles().contains(roleRepo.findByName("ROLE_ADMINISTRATOR"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
            }
            user.setLocked(true);
            userRepo.save(user);
            logRepository.save(new Log(null, LocalDateTime.now().toString(), "LOCK_USER",
                    details.getUsername(), "Lock user " + user.getEmail(), "/api/admin/user/access"));
            return new OneLineStatus(String.format("User %s locked!", changeAccess.getUser()));
        } else {
            user.setLocked(false);
            userRepo.save(user);
            logRepository.save(new Log(null, LocalDateTime.now().toString(), "UNLOCK_USER",
                    details.getUsername(), "Unlock user " + user.getEmail(), "/api/admin/user/access"));
            return new OneLineStatus(String.format("User %s unlocked!", changeAccess.getUser()));
        }
    }
}
