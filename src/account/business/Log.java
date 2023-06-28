package account.business;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Logs")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "log_generator")
    @SequenceGenerator(name = "log_generator", sequenceName = "logs_seq", allocationSize = 1)
    private Long id;

    private String date;
    private String action;
    private String subject;
    private String object;
    private String path;
}
