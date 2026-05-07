package backend.workspace.dto.WorkspaceMember;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Se usa en vez de lombok para que los datos sean inmutables
public record WorkspaceMemberRequest(

        @NotBlank(message = "El codigo del workspace es obligatorio")
        @Size(min = 8, max = 8, message = "El codigo debe tener exactamente 8 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9]{8}$", message = "El codigo solo puede contener letras y numeros")
        String code

) {}
