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
}
