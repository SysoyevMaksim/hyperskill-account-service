package account.business;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NewPassword {
    @JsonProperty("new_password")
    @NotBlank
    @Size(min = 12, message = "Password length must be 12 chars minimum!")
    private String password;
}
