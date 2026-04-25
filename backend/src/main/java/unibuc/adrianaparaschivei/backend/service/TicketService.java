package unibuc.adrianaparaschivei.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unibuc.adrianaparaschivei.backend.common.ClientIp;
import unibuc.adrianaparaschivei.backend.dto.TicketCreateRequestDto;
import unibuc.adrianaparaschivei.backend.dto.TicketUpdateRequestDto;
import unibuc.adrianaparaschivei.backend.model.Role;
import unibuc.adrianaparaschivei.backend.model.Ticket;
import unibuc.adrianaparaschivei.backend.model.TicketStatus;
import unibuc.adrianaparaschivei.backend.model.User;
import unibuc.adrianaparaschivei.backend.repository.TicketRepository;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final AuditService auditService;

    public TicketService(TicketRepository ticketRepository, AuditService auditService) {
        this.ticketRepository = ticketRepository;
        this.auditService = auditService;
    }

    public List<Ticket> listOwnTickets(User owner) {
        return ticketRepository.findByOwnerId(owner.getId());
    }

    public List<Ticket> search(String query) {
        String safeQuery = query == null ? "" : query;
        return ticketRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(safeQuery, safeQuery);
    }

    public Optional<Ticket> findById(UUID id) {
        return ticketRepository.findById(id);
    }

    public Ticket findViewableTicket(UUID id, User currentUser) throws AccessDeniedException {
        Ticket ticket = ticketRepository.findById(id).orElseThrow();
        if (!isOwner(ticket, currentUser)) {
            throw new AccessDeniedException("You can only view your own tickets");
        }
        return ticket;
    }

    public Ticket findEditableTicket(UUID id, User currentUser) throws AccessDeniedException {
        Ticket ticket = ticketRepository.findById(id).orElseThrow();
        if (!isOwner(ticket, currentUser) && !isManager(currentUser)) {
            throw new AccessDeniedException("You can only edit your own tickets unless you are a manager");
        }
        return ticket;
    }

    @Transactional
    public Ticket create(User owner, TicketCreateRequestDto request, HttpServletRequest httpRequest) {
        Ticket ticket = mapToTicket(owner, request);
        Ticket saved = ticketRepository.save(ticket);
        auditService.log(owner, "CREATE_TICKET", "ticket", saved.getId().toString(), ClientIp.from(httpRequest));
        return saved;
    }

    @Transactional
    public Ticket update(Ticket ticket, TicketUpdateRequestDto request, User actor, HttpServletRequest httpRequest) {
        applyUpdate(ticket, request);
        Ticket saved = ticketRepository.save(ticket);
        auditService.log(actor, "UPDATE_TICKET", "ticket", saved.getId().toString(), ClientIp.from(httpRequest));
        return saved;
    }

    private Ticket mapToTicket(User owner, TicketCreateRequestDto request) {
        Ticket ticket = new Ticket();
        ticket.setOwner(owner);
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setSeverity(request.severity());
        ticket.setStatus(TicketStatus.OPEN);
        return ticket;
    }

    private void applyUpdate(Ticket ticket, TicketUpdateRequestDto request) {
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setSeverity(request.severity());
        ticket.setStatus(request.status());
    }

    private boolean isOwner(Ticket ticket, User currentUser) {
        return ticket.getOwner().getId().equals(currentUser.getId());
    }

    private boolean isManager(User currentUser) {
        return currentUser.getRole() == Role.MANAGER;
    }
}
