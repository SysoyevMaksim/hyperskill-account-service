package account.present;

import account.business.Log;
import account.business.User;
import account.persistence.LogRepository;
import account.persistence.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@Service
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final Map<String, Integer> failures;

    private final LogRepository logRepository;
    private final UserRepository userRepo;

    public RestAuthenticationEntryPoint(ConcurrentMap<String, Integer> failures, LogRepository logRepository, UserRepository userRepo) {
        this.failures = failures;
        this.logRepository = logRepository;
        this.userRepo = userRepo;
    }


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        OutputStream responseStream = response.getOutputStream();
        if (request.getHeader("Authorization") != null) {
            String loginPassword = new String(Base64.getDecoder().decode(request.getHeader("Authorization")
                    .substring(6)));
            String login = loginPassword.substring(0, loginPassword.indexOf(':'));
            User user = userRepo.findByEmail(login);
            if (user == null) {
                logRepository.save(new Log(null, LocalDateTime.now().toString(),
                        "LOGIN_FAILED", login, request.getRequestURI(), request.getRequestURI()));
            }
            else if (!user.isLocked()) {
                logRepository.save(new Log(null, LocalDateTime.now().toString(),
                        "LOGIN_FAILED", login, request.getRequestURI(), request.getRequestURI()));
                if (failures.containsKey(login)) {
                    failures.put(login, failures.get(login) + 1);
                } else {
                    failures.put(login, 1);
                }
                if (failures.get(login) == 5) {
                    user.setLocked(true);
                    userRepo.save(user);
                    logRepository.save(new Log(null, LocalDateTime.now().toString(), "BRUTE_FORCE",
                            login, request.getRequestURI(), request.getRequestURI()));
                    logRepository.save(new Log(null, LocalDateTime.now().toString(), "LOCK_USER",
                            login, "Lock user " + login, request.getRequestURI()));
                }
            }
        }
        new ObjectMapper().writeValue(responseStream, new CustomErrorMessage(LocalDateTime.now().toString(),
                HttpStatus.UNAUTHORIZED.value(), "Unauthorized", authException.getMessage(), request.getRequestURI()));
        responseStream.flush();
    }
}
