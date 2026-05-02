package tn.esprit.projetintegre.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.projetintegre.dto.ChatRoomDTO;
import tn.esprit.projetintegre.entities.ChatRoom;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.enums.ChatRoomType;
import tn.esprit.projetintegre.exception.BusinessException;
import tn.esprit.projetintegre.repositories.ChatRoomRepository;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    private User creator;

    @BeforeEach
    void setUp() {
        creator = User.builder().id(1L).name("Creator").build();
    }

    @Test
    void createRoom_shouldAddCreatorToMembersAndAdmins() {
        ChatRoomDTO.CreateRequest request = ChatRoomDTO.CreateRequest.builder()
                .name("Room A")
                .description("Desc")
                .type(ChatRoomType.GROUP)
                .memberIds(List.of(2L))
                .build();

        User extraMember = User.builder().id(2L).name("Member").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(userRepository.findAllById(List.of(2L))).thenReturn(List.of(extraMember));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> {
            ChatRoom room = invocation.getArgument(0);
            room.setId(10L);
            return room;
        });

        ChatRoomDTO.Response response = chatRoomService.createRoom(1L, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Room A", response.getName());
        assertEquals(1L, response.getCreatorId());
        assertEquals("Creator", response.getCreatorName());
        assertEquals(2, response.getMemberCount());
        assertTrue(response.getIsActive());
    }

    @Test
    void updateRoom_shouldThrow_whenUserIsNotAdmin() {
        User anotherUser = User.builder().id(7L).name("Other").build();
        ChatRoom room = ChatRoom.builder()
                .id(10L)
                .name("Old")
                .admins(new ArrayList<>(List.of(anotherUser)))
                .members(new ArrayList<>())
                .creator(creator)
                .build();

        ChatRoomDTO.UpdateRequest request = ChatRoomDTO.UpdateRequest.builder().name("New").build();
        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));

        assertThrows(BusinessException.class, () -> chatRoomService.updateRoom(10L, 1L, request));
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    void addMember_shouldThrow_whenRoomIsFull() {
        User admin = User.builder().id(1L).name("Admin").build();
        User member = User.builder().id(2L).name("Member").build();

        ChatRoom room = ChatRoom.builder()
                .id(10L)
                .maxMembers(1)
                .allowJoin(true)
                .admins(new ArrayList<>(List.of(admin)))
                .members(new ArrayList<>(List.of(member)))
                .creator(admin)
                .build();

        when(chatRoomRepository.findByIdWithMembers(10L)).thenReturn(Optional.of(room));

        assertThrows(BusinessException.class, () -> chatRoomService.addMember(10L, 1L, 3L));
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    void deleteRoom_shouldThrow_whenUserIsNotCreator() {
        ChatRoom room = ChatRoom.builder().id(10L).creator(creator).build();
        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));

        assertThrows(BusinessException.class, () -> chatRoomService.deleteRoom(10L, 99L));
        verify(chatRoomRepository, never()).delete(any(ChatRoom.class));
    }
}

