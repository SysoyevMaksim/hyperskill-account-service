package account.present;

import account.business.Log;
import account.persistence.LogRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AuditorController {

    private final LogRepository logRepository;

    public AuditorController(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @GetMapping("/api/security/events/")
    List<Log> getEvents(@AuthenticationPrincipal UserDetails details) {
        System.out.println(details.getAuthorities());
        return logRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }
}
