package pk.rd.pasir_drozd_roksana.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}