package unibuc.adrianaparaschivei.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unibuc.adrianaparaschivei.backend.common.ClientIp;
import unibuc.adrianaparaschivei.backend.dto.TicketCreateRequestDto;
import unibuc.adrianaparaschivei.backend.dto.TicketUpdateRequestDto;
import unibuc.adrianaparaschivei.backend.exceptions.AccessForbiddenException;
import unibuc.adrianaparaschivei.backend.exceptions.BadRequestException;
import unibuc.adrianaparaschivei.backend.exceptions.ResourceNotFoundException;
import unibuc.adrianaparaschivei.backend.model.Role;
import unibuc.adrianaparaschivei.backend.model.Ticket;
import unibuc.adrianaparaschivei.backend.model.TicketStatus;
import unibuc.adrianaparaschivei.backend.model.User;
import unibuc.adrianaparaschivei.backend.repository.TicketRepository;
import unibuc.adrianaparaschivei.backend.repository.TicketSearchRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketService {
    private static final int MIN_TITLE_LENGTH = 3;
    private static final int MAX_TITLE_LENGTH = 120;
    private static final int MAX_DESCRIPTION_LENGTH = 2_000;
    private static final int MAX_SEARCH_LENGTH = 100;
    private static final int SEARCH_RESULT_LIMIT = 20;

    private final TicketRepository ticketRepository;
    private final TicketSearchRepository ticketSearchRepository;
    private final AuditService auditService;

    public TicketService(TicketRepository ticketRepository, TicketSearchRepository ticketSearchRepository, AuditService auditService) {
        this.ticketRepository = ticketRepository;
        this.ticketSearchRepository = ticketSearchRepository;
        this.auditService = auditService;
    }

    public List<Ticket> listVisibleTickets(User actor) {
        if (isManager(actor)) {
            return ticketRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, SEARCH_RESULT_LIMIT));
        }
        return ticketRepository.findByOwnerIdOrderByCreatedAtDesc(actor.getId());
    }

    public List<Ticket> search(User actor, String query, HttpServletRequest request) {
        String safeQuery = validateSearchQuery(query);
        List<Ticket> results;

        if (isManager(actor)) {
            results = ticketSearchRepository.searchAllTickets(safeQuery, SEARCH_RESULT_LIMIT);
        } else {
            results = ticketSearchRepository.searchOwnTickets(actor.getId(), safeQuery, SEARCH_RESULT_LIMIT);
        }

        auditService.log(actor, "SEARCH_TICKETS", "ticket", safeQuery, ClientIp.from(request));
        return results;
    }

    public Optional<Ticket> findById(UUID id) {
        return ticketRepository.findById(id);
    }

    public Ticket findViewableTicket(UUID id, User currentUser) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket was not found"));
        if (!isOwner(ticket, currentUser) && !isManager(currentUser)) {
            throw new AccessForbiddenException("You can only view your own tickets unless you are a manager");
        }
        return ticket;
    }

    public Ticket findEditableTicket(UUID id, User currentUser) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket was not found"));
        if (!isOwner(ticket, currentUser) && !isManager(currentUser)) {
            throw new AccessForbiddenException("You can only edit your own tickets unless you are a manager");
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
        applyStatusUpdate(ticket, request.status(), actor);
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
        ticket.setTitle(validateTitle(request.title()));
        ticket.setDescription(validateDescription(request.description()));
        ticket.setSeverity(request.severity());
        ticket.setStatus(TicketStatus.OPEN);
        return ticket;
    }

    private void applyUpdate(Ticket ticket, TicketUpdateRequestDto request) {
        ticket.setTitle(validateTitle(request.title()));
        ticket.setDescription(validateDescription(request.description()));
        ticket.setSeverity(request.severity());
    }

    private void applyStatusUpdate(Ticket ticket, TicketStatus requestedStatus, User actor) {
        if (ticket.getStatus() == requestedStatus) {
            return;
        }

        if (!isManager(actor)) {
            throw new AccessForbiddenException("Only managers can change ticket status");
        }

        ticket.setStatus(requestedStatus);
    }

    private String validateTitle(String title) {
        if (title == null) {
            throw new BadRequestException("Title is required");
        }

        String cleanTitle = title.trim();
        if (cleanTitle.length() < MIN_TITLE_LENGTH || cleanTitle.length() > MAX_TITLE_LENGTH) {
            throw new BadRequestException("Title must have between 3 and 120 characters");
        }
        return cleanTitle;
    }

    private String validateDescription(String description) {
        if (description == null) {
            throw new BadRequestException("Description is required");
        }

        String cleanDescription = description.trim();
        if (cleanDescription.isBlank()) {
            throw new BadRequestException("Description is required");
        }
        if (cleanDescription.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BadRequestException("Description cannot have more than 2000 characters");
        }
        return cleanDescription;
    }

    private String validateSearchQuery(String query) {
        if (query == null) {
            return "";
        }

        String cleanQuery = query.trim();
        if (cleanQuery.length() > MAX_SEARCH_LENGTH) {
            throw new BadRequestException("Search query cannot have more than 100 characters");
        }
        return cleanQuery;
    }

    private boolean isOwner(Ticket ticket, User currentUser) {
        return ticket.getOwner().getId().equals(currentUser.getId());
    }

    private boolean isManager(User currentUser) {
        return currentUser.getRole() == Role.MANAGER;
    }
}
