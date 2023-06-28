package account.present;

import account.business.Payment;
import account.business.User;
import account.persistence.PaymentRepository;
import account.persistence.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
public class EmployeeController {

    private final UserRepository userRepo;
    private final PaymentRepository paymentRepo;

    public EmployeeController(UserRepository userRepo, PaymentRepository paymentRepo) {
        this.userRepo = userRepo;
        this.paymentRepo = paymentRepo;
    }


    @GetMapping(value = "/api/empl/payment")
    ResponseEntity<?> paymentOne(@AuthenticationPrincipal UserDetails details, @RequestParam(required = false) String period) throws ParseException {
        if (period == null) {
            List<Payment> payments = paymentRepo.findByEmployeeOrderByPeriodDesc(userRepo.findByEmail(details.getUsername()).getId());
            for (Payment payment : payments) {
                fillPayment(payment);
            }
            return new ResponseEntity<>(payments, HttpStatus.OK);
        }
        if (Integer.parseInt(period.substring(0, 2)) > 12
                || Integer.parseInt(period.substring(0, 2)) < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment");
        }
        Payment payment = paymentRepo.findByEmployeeAndPeriod(userRepo.findByEmail(details.getUsername()).getId(), period);
        fillPayment(payment);
        return new ResponseEntity<>(payment, HttpStatus.OK);
    }

    private void fillPayment(Payment payment) throws ParseException {
        Optional<User> user = userRepo.findById(payment.getEmployee());
        SimpleDateFormat from = new SimpleDateFormat("MM-yyyy");
        SimpleDateFormat to = new SimpleDateFormat("MMMM-yyyy", Locale.ENGLISH);
        if (user.isPresent()) {
            payment.setName(user.get().getName());
            payment.setLastname(user.get().getLastname());
            payment.setPeriod(to.format(from.parse(payment.getPeriod())));
            payment.setStringSalary(String.format("%d dollar(s) %d cent(s)",
                    payment.getSalary() / 100, payment.getSalary() % 100));
        }
    }
}
