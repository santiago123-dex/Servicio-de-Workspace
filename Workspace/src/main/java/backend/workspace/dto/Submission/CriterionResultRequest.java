package backend.workspace.dto.Submission;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CriterionResultRequest(
        @NotBlank(message = "criterionId es obligatorio")
        String criterionId,

        @NotNull(message = "score por criterio es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "score por criterio no puede ser negativo")
        BigDecimal score,

        @NotBlank(message = "feedback por criterio es obligatorio")
        String feedback
) {
}
