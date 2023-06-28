package account.present;

import account.business.Payment;
import account.business.OneLineStatus;
import account.business.User;
import account.persistence.PaymentRepository;
import account.persistence.UserRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Validated
@RestController
public class AccountController {
    private final UserRepository userRepo;

    private final PaymentRepository paymentRepo;

    public AccountController(UserRepository userRepo, PaymentRepository paymentRepo) {
        this.userRepo = userRepo;
        this.paymentRepo = paymentRepo;
    }

    @PostMapping(value = "/api/acct/payments")
    OneLineStatus AddPayments(@RequestBody @Valid List<Payment> payments) {
        try {
            for (Payment payment : payments) {
                fillPayment(payment);
            }
            paymentRepo.saveAll(payments);
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment");
        }
        return new OneLineStatus("Added successfully!");
    }

    @PutMapping(value = "/api/acct/payments")
    OneLineStatus UpdatePayment(@RequestBody @Valid Payment payment) {
        try {
            fillPayment(payment);
            payment.setId(paymentRepo.findByEmployeeAndPeriod(payment.getEmployee(), payment.getPeriod()).getId());
            paymentRepo.save(payment);
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment");
        }
        return new OneLineStatus("Updated successfully!");
    }

    private void fillPayment(Payment payment) {
        User user = userRepo.findByEmail(payment.getEmail());
        payment.setEmployee(user.getId());
        if (Integer.parseInt(payment.getPeriod().substring(0, 2)) > 12
                || Integer.parseInt(payment.getPeriod().substring(0, 2)) < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment");
        }
    }
}
