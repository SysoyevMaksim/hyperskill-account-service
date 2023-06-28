package account.present;

import account.business.Log;
import account.business.NewPassword;
import account.business.User;
import account.business.UserStatus;
import account.persistence.LogRepository;
import account.persistence.RoleRepository;
import account.persistence.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
public class AuthController {

    private final UserRepository userRepo;

    private final LogRepository logRepository;

    private final RoleRepository roleRepo;

    private final PasswordEncoder encoder;

    private final Object RoleIdentifyingLock = new Object();
    private final Set<String> breachedPasswords;

    public AuthController(UserRepository userRepo, LogRepository logRepository, RoleRepository roleRepo, PasswordEncoder encoder,
                          @Qualifier("breachedPasswords") Set<String> breachedPasswords) {
        this.userRepo = userRepo;
        this.logRepository = logRepository;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
        this.breachedPasswords = breachedPasswords;
    }

    @PostMapping(value = "/api/auth/signup")
    User signup(@RequestBody @Valid User user) {
        if (user.getName() == null || user.getLastname() == null
                || user.getEmail() == null || user.getPassword() == null
                || user.getName().equals("") || user.getLastname().equals("")
                || user.getEmail().equals("") || user.getPassword().equals("")
                || !user.getEmail().endsWith("@acme.com")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        user.setEmail(user.getEmail().toLowerCase());
        if (userRepo.findByEmail(user.getEmail()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");
        }
        if (breachedPasswords.contains(user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        }
        user.setPassword(encoder.encode(user.getPassword()));
        synchronized (RoleIdentifyingLock) {
            if (roleRepo.getNumberOfUse("ROLE_ADMINISTRATOR") == 0) {
                user.setRoles(List.of(roleRepo.findByName("ROLE_ADMINISTRATOR")));
            } else {
                user.setRoles(List.of(roleRepo.findByName("ROLE_USER")));
            }
        }
        userRepo.save(user);
        user.setStringRoles(List.of(user.getRoles().get(0).getName()));
        logRepository.save(new Log(null, LocalDateTime.now().toString(),
                "CREATE_USER", "Anonymous", user.getEmail(), "/api/auth/signup"));
        return user;
    }

    @PostMapping(value = "api/auth/changepass")
    UserStatus changePass(@RequestBody @Valid NewPassword newPassword,
                          @AuthenticationPrincipal UserDetails details) {
        User user = userRepo.findByEmail(details.getUsername());
        if (breachedPasswords.contains(newPassword.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        }
        if (encoder.matches(newPassword.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
        }
        user.setPassword(encoder.encode(newPassword.getPassword()));
        userRepo.save(user);
        logRepository.save(new Log(null, LocalDateTime.now().toString(),
                "CHANGE_PASSWORD", user.getEmail(), user.getEmail(), "/api/auth/changepass"));
        return new UserStatus(user.getEmail(), "The password has been updated successfully");
    }
}
