package backend.workspace.repository;

import backend.workspace.entity.Workspace;
import backend.workspace.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Integer> {

    @Query(value = "select * from workspace w where w.data ->> 'encodedCode' = :encodedCode", nativeQuery = true)
    Optional<Workspace> findByEncodedCode(@Param("encodedCode") String encodedCode);

    //Busca todos los workspaces de un usuario
    List<Workspace> findByOwnerUserID(UUID userId);


    // Trae los workspaces en los que el user es admin o es miembro
    @Query(
            value = """
                    SELECT w.* FROM workspace_member wm
                    JOIN workspace w ON w.id = wm.workspace_id
                    WHERE wm.user_id = :userId
                    """, nativeQuery = true
    )
    List<Workspace> getAllWorkspaces(@Param("userId") UUID userId);
}
