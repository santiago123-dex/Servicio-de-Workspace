package backend.workspace.dto.Submission;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TeacherRequest(
        @NotNull(message = "El adminUserId es obligatorio")
        UUID adminUserId,

        @NotNull(message = "La nota del profesor es obligatoria")
        @DecimalMin(value = "0.0", inclusive = true, message = "La nota no puede ser negativa")
        BigDecimal score,

        @NotBlank(message = "La retroalimentación es obligatoria")
        String feedback
) {}
