package account.persistence;

import account.business.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByEmployeeAndPeriod(Long id, String period);

    List<Payment> findByEmployeeOrderByPeriodDesc(Long id);
}
