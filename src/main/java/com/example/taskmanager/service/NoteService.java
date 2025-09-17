package com.example.taskmanager.service;

import com.example.taskmanager.dto.NoteRequest;
import com.example.taskmanager.dto.NoteResponse;
import com.example.taskmanager.model.Note;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.NoteRepository;
import com.example.taskmanager.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserService userService;

    private NoteResponse mapToNoteResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    private String getAuthenticatedUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        } else {
            throw new RuntimeException("Unable to get authenticated username");
        }
    }

    public NoteResponse createNote(NoteRequest noteRequest) {
        String username = getAuthenticatedUsername();
        User user = userService.findByUsername(username);

        Note note = new Note();
        note.setTitle(noteRequest.getTitle());
        note.setContent(noteRequest.getContent());
        note.setUser(user);

        Note savedNote = noteRepository.save(note);
        return mapToNoteResponse(savedNote);
    }

    public List<NoteResponse> getAllNotesForUser() {
        String username = getAuthenticatedUsername();
        User user = userService.findByUsername(username);

        return noteRepository.findByUserOrderByUpdatedAtDesc(user)
                .stream()
                .map(this::mapToNoteResponse)
                .collect(Collectors.toList());
    }

    public NoteResponse getNoteById(Long noteId) {
        String username = getAuthenticatedUsername();
        User user = userService.findByUsername(username);

        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        return mapToNoteResponse(note);
    }

    public NoteResponse updateNote(Long noteId, NoteRequest noteRequest) {
        String username = getAuthenticatedUsername();
        User user = userService.findByUsername(username);

        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        note.setTitle(noteRequest.getTitle());
        note.setContent(noteRequest.getContent());

        Note updatedNote = noteRepository.save(note);
        return mapToNoteResponse(updatedNote);
    }

    public void deleteNote(Long noteId) {
        String username = getAuthenticatedUsername();
        User user = userService.findByUsername(username);

        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        noteRepository.delete(note);
    }
}