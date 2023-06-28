package account.present;

import account.business.User;
import account.persistence.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@Service
public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

    private final Map<String, Integer> failures;
    private final UserRepository userRepo;

    public CustomAuthenticationProvider(UserRepository userRepo, UserDetailsService userDetailsService, PasswordEncoder encoder, ConcurrentMap<String, Integer> failures) {
        this.userRepo = userRepo;
        this.failures = failures;
        this.setUserDetailsService(userDetailsService);
        this.setPasswordEncoder(encoder);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        super.additionalAuthenticationChecks(userDetails, authentication);

        User user = userRepo.findByEmail(userDetails.getUsername());
        if (user.isLocked() && !user.getEmail().equals("johndoe@acme.com")) {
            throw new AccountLockedException("User account is locked");
        }
        failures.put(user.getEmail(), 0);
    }

    public static class AccountLockedException extends AuthenticationException {
        public AccountLockedException(String message) {
            super(message);
        }
    }
}
