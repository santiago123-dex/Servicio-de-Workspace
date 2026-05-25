package backend.workspace.dto.Submission;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record AiGradeRequest(
        @NotNull(message = "La nota de IA es obligatoria")
        @DecimalMin(value = "0.0", inclusive = true, message = "La nota no puede ser negativa")
        BigDecimal score,

        @NotBlank(message = "La retroalimentación de IA es obligatoria")
        String feedback,

        List<CriterionResultRequest> rubricResults,

        String evaluatedAt
) {}
