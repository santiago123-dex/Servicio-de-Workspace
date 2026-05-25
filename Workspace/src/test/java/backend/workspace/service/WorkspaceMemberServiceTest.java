package backend.workspace.service;

import backend.workspace.client.UserServiceClient;
import backend.workspace.dto.User.UserSummaryResponse;
import backend.workspace.dto.WorkspaceMember.WorkspaceMemberDetailsResponse;
import backend.workspace.dto.WorkspaceMember.WorkspaceMemberRequest;
import backend.workspace.dto.WorkspaceMember.WorkspaceMemberResponse;
import backend.workspace.entity.Workspace;
import backend.workspace.entity.WorkspaceMember;
import backend.workspace.exception.Workspace.WorkspaceNotFoundException;
import backend.workspace.exception.WorkspaceMember.MemberAlreadyExistException;
import backend.workspace.exception.WorkspaceMember.MemberNotFoundException;
import backend.workspace.repository.WorkspaceMemberRepository;
import backend.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceMemberServiceTest {

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private WorkspaceCodeCodec workspaceCodeCodec;

    @InjectMocks
    private WorkspaceMemberService workspaceMemberService;

    private Workspace workspace;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        workspace = Workspace.builder()
                .id(1)
                .name("Workspace de prueba")
                .description("Descripcion")
                .status(Workspace.WorkspaceStatus.ACTIVO)
                .members(new ArrayList<>())
                .build();
    }

    @Test
    void shouldAddMemberWhenCodeExistAndUserIsNotMember() {
        WorkspaceMemberRequest request = new WorkspaceMemberRequest("ABC12345");

        when(workspaceCodeCodec.encode("ABC12345")).thenReturn("QUJDMTIzNDU=");
        when(workspaceRepository.findByEncodedCode("QUJDMTIzNDU="))
                .thenReturn(Optional.of(workspace));
        when(workspaceMemberRepository.existsByWorkspaceIdAndUserId(1, userId))
                .thenReturn(false);

        WorkspaceMember savedMember = WorkspaceMember.builder()
                .id(10)
                .userId(userId)
                .role(WorkspaceMember.Role.MEMBER)
                .workspace(workspace)
                .build();

        when(workspaceMemberRepository.save(any(WorkspaceMember.class)))
                .thenReturn(savedMember);

        WorkspaceMemberResponse response = workspaceMemberService.addMember(userId, request);

        assertEquals(10, response.id());
        assertEquals(1, response.workspaceId());
        assertEquals(userId, response.userId());
        assertEquals(WorkspaceMember.Role.MEMBER, response.role());
        assertEquals(1, workspace.getMembers().size());

        verify(workspaceRepository).findByEncodedCode("QUJDMTIzNDU=");
        verify(workspaceMemberRepository).existsByWorkspaceIdAndUserId(1, userId);
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
    }

    @Test
    void shouldThrowWhenWorkspaceCodeDoesNotExist() {
        WorkspaceMemberRequest request = new WorkspaceMemberRequest("MISS1234");
        when(workspaceCodeCodec.encode("MISS1234")).thenReturn("TUlTUzEyMzQ=");
        when(workspaceRepository.findByEncodedCode("TUlTUzEyMzQ=")).thenReturn(Optional.empty());

        assertThrows(WorkspaceNotFoundException.class, () -> workspaceMemberService.addMember(userId, request));
        verify(workspaceMemberRepository, never()).save(any(WorkspaceMember.class));
    }

    @Test
    void shouldThrowWhenMemberAlreadyExists() {
        WorkspaceMemberRequest request = new WorkspaceMemberRequest("ABC12345");
        when(workspaceCodeCodec.encode("ABC12345")).thenReturn("QUJDMTIzNDU=");
        when(workspaceRepository.findByEncodedCode("QUJDMTIzNDU=")).thenReturn(Optional.of(workspace));
        when(workspaceMemberRepository.existsByWorkspaceIdAndUserId(1, userId)).thenReturn(true);

        assertThrows(MemberAlreadyExistException.class, () -> workspaceMemberService.addMember(userId, request));
        verify(workspaceMemberRepository, never()).save(any(WorkspaceMember.class));
    }

    @Test
    void shouldAddOwnerAsAdmin() {
        workspaceMemberService.addOwnerAsAdmin(workspace, userId);

        assertEquals(1, workspace.getMembers().size());
        assertEquals(WorkspaceMember.Role.ADMIN, workspace.getMembers().getFirst().getRole());
        assertEquals(userId, workspace.getMembers().getFirst().getUserId());
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
    }

    @Test
    void shouldReturnMembersByWorkspace() {
        WorkspaceMember member = WorkspaceMember.builder()
                .id(20)
                .workspace(workspace)
                .userId(userId)
                .role(WorkspaceMember.Role.MEMBER)
                .build();
        when(workspaceRepository.findById(1)).thenReturn(Optional.of(workspace));
        when(workspaceMemberRepository.findByWorkspaceId(1)).thenReturn(List.of(member));

        List<WorkspaceMemberResponse> response = workspaceMemberService.getMembersByWorkspace(1);

        assertEquals(1, response.size());
        assertEquals(20, response.getFirst().id());
        verify(workspaceRepository).findById(1);
        verify(workspaceMemberRepository).findByWorkspaceId(1);
    }

    @Test
    void shouldDeleteMemberFromWorkspaceAndRepository() {
        WorkspaceMember member = WorkspaceMember.builder()
                .id(22)
                .workspace(workspace)
                .userId(userId)
                .role(WorkspaceMember.Role.MEMBER)
                .build();
        workspace.getMembers().add(member);
        when(workspaceMemberRepository.findById(22)).thenReturn(Optional.of(member));

        workspaceMemberService.deleteMember(22);

        assertEquals(0, workspace.getMembers().size());
        verify(workspaceMemberRepository).delete(member);
    }

    @Test
    void shouldReturnMembersDetailsByWorkspace() {
        UUID secondUserId = UUID.randomUUID();

        WorkspaceMember firstMember = WorkspaceMember.builder()
                .id(20)
                .workspace(workspace)
                .userId(userId)
                .role(WorkspaceMember.Role.ADMIN)
                .build();

        WorkspaceMember secondMember = WorkspaceMember.builder()
                .id(21)
                .workspace(workspace)
                .userId(secondUserId)
                .role(WorkspaceMember.Role.MEMBER)
                .build();

        when(workspaceRepository.findById(1)).thenReturn(Optional.of(workspace));
        when(workspaceMemberRepository.findByWorkspaceId(1)).thenReturn(List.of(firstMember, secondMember));
        when(userServiceClient.getUserSummaries(List.of(userId, secondUserId))).thenReturn(List.of(
                new UserSummaryResponse(userId, "John", "Doe", "John Doe", "https://cdn/john.png"),
                new UserSummaryResponse(secondUserId, "Jane", "Smith", "Jane Smith", "https://cdn/jane.png")
        ));

        List<WorkspaceMemberDetailsResponse> response = workspaceMemberService.getMembersDetailsByWorkspace(1);

        assertEquals(2, response.size());
        assertEquals("John Doe", response.get(0).fullName());
        assertEquals("https://cdn/jane.png", response.get(1).avatarUrl());
        verify(userServiceClient).getUserSummaries(List.of(userId, secondUserId));
    }

    @Test
    void shouldThrowWhenDeletingUnknownMember() {
        when(workspaceMemberRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> workspaceMemberService.deleteMember(999));
        verify(workspaceMemberRepository, never()).delete(any(WorkspaceMember.class));
    }
}
