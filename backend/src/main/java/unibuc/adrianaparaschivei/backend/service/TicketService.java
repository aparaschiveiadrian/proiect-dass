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
import unibuc.adrianaparaschivei.backend.repository.VulnerableTicketSqlRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final VulnerableTicketSqlRepository vulnerableTicketSqlRepository;
    private final AuditService auditService;

    public TicketService(TicketRepository ticketRepository, VulnerableTicketSqlRepository vulnerableTicketSqlRepository, AuditService auditService) {
        this.ticketRepository = ticketRepository;
        this.vulnerableTicketSqlRepository = vulnerableTicketSqlRepository;
        this.auditService = auditService;
    }

    public List<Ticket> listVisibleTicketsVulnerable(User actor) {
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Ticket> searchVulnerable(String query) {
        String unsafeQuery = query == null ? "" : query;
        return vulnerableTicketSqlRepository.searchUnsafe(unsafeQuery);
    }

    public Optional<Ticket> findById(UUID id) {
        return ticketRepository.findById(id);
    }

    public Ticket findViewableTicket(UUID id, User currentUser) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow();
        return ticket;
    }

    public Ticket findEditableTicket(UUID id, User currentUser) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow();
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

    @Transactional
    public void delete(Ticket ticket, User actor, HttpServletRequest httpRequest) {
        UUID ticketId = ticket.getId();
        ticketRepository.delete(ticket);
        auditService.log(actor, "DELETE_TICKET", "ticket", ticketId.toString(), ClientIp.from(httpRequest));
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
