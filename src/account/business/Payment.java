package account.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payments_gen")
    @SequenceGenerator(name = "payments_gen", sequenceName = "payments_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    @JsonIgnore
    private Long id;

    @JsonIgnore
    private Long employee;

//    @NotBlank
    @Transient
    @JsonProperty(value = "employee", access = JsonProperty.Access.WRITE_ONLY)
    private String email;

    @Transient
    private String name;
    @Transient
    private String lastname;

    @NotNull
    private String period;
    @NotNull
    @Min(0)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long salary;

    @JsonProperty(value = "salary", access = JsonProperty.Access.READ_ONLY)
    @Transient
    private String stringSalary;
}
