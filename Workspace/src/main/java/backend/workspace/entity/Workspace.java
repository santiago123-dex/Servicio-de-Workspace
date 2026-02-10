package backend.workspace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "workspace")
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "owner_user_id", nullable = false)
    private Integer ownerUserID;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private WorkspaceStatus status;

    public enum WorkspaceStatus{
        BORRADOR,
        ACTIVO,
        ARCHIVADO;

        //Devuelve el nombre del enum para representarlo en JSON de salida
        @JsonValue
        public String getName(){
            return this.name();
        }

        //Recibe el valor de json y lo convierte a mayuscula para que coincida con el enum
        @JsonCreator
        public static WorkspaceStatus fromString(String value){
            return WorkspaceStatus.valueOf(value.toUpperCase());
        }
    }

    //Recibe el json y lo almacena como map antes de guardarlo en la base de datos
    //todo lo que es conversion lo hace hibernate osea de map a Jsonb o de jsonb a map

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb")
    private Map<String, Object> data;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist(){
        this.createdAt = OffsetDateTime.now();
    }

}
