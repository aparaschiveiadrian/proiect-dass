package unibuc.adrianaparaschivei.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import unibuc.adrianaparaschivei.backend.common.ClientIp;
import unibuc.adrianaparaschivei.backend.common.CurrentUserProvider;
import unibuc.adrianaparaschivei.backend.dto.AuthResultDto;
import unibuc.adrianaparaschivei.backend.dto.TicketCreateRequestDto;
import unibuc.adrianaparaschivei.backend.dto.TicketResponseDto;
import unibuc.adrianaparaschivei.backend.dto.TicketUpdateRequestDto;
import unibuc.adrianaparaschivei.backend.model.Role;
import unibuc.adrianaparaschivei.backend.model.Ticket;
import unibuc.adrianaparaschivei.backend.model.TicketSeverity;
import unibuc.adrianaparaschivei.backend.model.TicketStatus;
import unibuc.adrianaparaschivei.backend.model.User;
import unibuc.adrianaparaschivei.backend.service.AuditService;
import unibuc.adrianaparaschivei.backend.service.TicketService;

import java.util.List;
import java.util.UUID;

@Controller
public class TicketController {
    private final TicketService ticketService;
    private final CurrentUserProvider currentUserProvider;
    private final AuditService auditService;

    public TicketController(TicketService ticketService, CurrentUserProvider currentUserProvider, AuditService auditService) {
        this.ticketService = ticketService;
        this.currentUserProvider = currentUserProvider;
        this.auditService = auditService;
    }

    @GetMapping("/tickets")
    public String list(HttpServletRequest request, Model model) {
        User user = currentUser(request);
        model.addAttribute("tickets", ticketService.listVisibleTickets(user));
        addCurrentUserToModel(model, user);
        return "tickets/list";
    }

    @ResponseBody
    @GetMapping("/api/tickets")
    public List<TicketResponseDto> apiList(HttpServletRequest request) {
        User user = currentUser(request);
        return ticketService.listVisibleTickets(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/tickets/search")
    public String search(@RequestParam(defaultValue = "") String q, HttpServletRequest request, Model model) {
        User user = currentUser(request);
        model.addAttribute("tickets", ticketService.search(user, q, request));
        model.addAttribute("query", q);
        addCurrentUserToModel(model, user);
        return "tickets/list";
    }

    @ResponseBody
    @GetMapping("/api/tickets/search")
    public List<TicketResponseDto> apiSearch(@RequestParam(defaultValue = "") String q, HttpServletRequest request) {
        User user = currentUser(request);
        return ticketService.search(user, q, request).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/tickets/new")
    public String newForm(Model model) {
        model.addAttribute("severities", TicketSeverity.values());
        return "tickets/new";
    }

    @PostMapping("/tickets")
    public String create(@RequestParam String title, @RequestParam String description, @RequestParam TicketSeverity severity, HttpServletRequest request) {
        User user = currentUser(request);
        TicketCreateRequestDto createRequest = new TicketCreateRequestDto(title, description, severity);
        Ticket ticket = ticketService.create(user, createRequest, request);
        return "redirect:/tickets/" + ticket.getId();
    }

    @ResponseBody
    @PostMapping("/api/tickets")
    public TicketResponseDto apiCreate(@RequestBody TicketCreateRequestDto createRequest, HttpServletRequest request) {
        User user = currentUser(request);
        Ticket ticket = ticketService.create(user, createRequest, request);
        return toResponse(ticket);
    }

    @GetMapping("/tickets/{id}")
    public String details(@PathVariable UUID id, HttpServletRequest request, Model model) {
        User actor = currentUser(request);
        Ticket ticket = ticketService.findViewableTicket(id, actor);
        auditService.log(actor, "VIEW_TICKET", "ticket", ticket.getId().toString(), ClientIp.from(request));
        model.addAttribute("ticket", ticket);
        model.addAttribute("actor", actor);
        model.addAttribute("canManageAllTickets", isManager(actor));
        return "tickets/details";
    }

    @ResponseBody
    @GetMapping("/api/tickets/{id}")
    public TicketResponseDto apiDetails(@PathVariable UUID id, HttpServletRequest request) {
        User actor = currentUser(request);
        Ticket ticket = ticketService.findViewableTicket(id, actor);
        auditService.log(actor, "VIEW_TICKET", "ticket", ticket.getId().toString(), ClientIp.from(request));
        return toResponse(ticket);
    }

    @GetMapping("/tickets/{id}/edit")
    public String editForm(@PathVariable UUID id, HttpServletRequest request, Model model) {
        User actor = currentUser(request);
        Ticket ticket = ticketService.findEditableTicket(id, actor);
        model.addAttribute("ticket", ticket);
        model.addAttribute("severities", TicketSeverity.values());
        model.addAttribute("statuses", TicketStatus.values());
        model.addAttribute("canChangeStatus", isManager(actor));
        return "tickets/edit";
    }

    @PostMapping("/tickets/{id}/edit")
    public String edit(@PathVariable UUID id, @RequestParam String title, @RequestParam String description, @RequestParam TicketSeverity severity, @RequestParam TicketStatus status, HttpServletRequest request) {
        User actor = currentUser(request);
        Ticket ticket = ticketService.findEditableTicket(id, actor);
        TicketUpdateRequestDto updateRequest = new TicketUpdateRequestDto(title, description, severity, status);
        ticketService.update(ticket, updateRequest, actor, request);
        return "redirect:/tickets/" + id;
    }

    @ResponseBody
    @PutMapping("/api/tickets/{id}")
    public TicketResponseDto apiEdit(@PathVariable UUID id, @RequestBody TicketUpdateRequestDto updateRequest, HttpServletRequest request) {
        User actor = currentUser(request);
        Ticket ticket = ticketService.findEditableTicket(id, actor);
        Ticket saved = ticketService.update(ticket, updateRequest, actor, request);
        return toResponse(saved);
    }

    @PostMapping("/tickets/{id}/delete")
    public String delete(@PathVariable UUID id, HttpServletRequest request) {
        User actor = currentUser(request);
        Ticket ticket = ticketService.findEditableTicket(id, actor);
        ticketService.delete(ticket, actor, request);
        return "redirect:/tickets";
    }

    @ResponseBody
    @DeleteMapping("/api/tickets/{id}")
    public AuthResultDto apiDelete(@PathVariable UUID id, HttpServletRequest request) {
        User actor = currentUser(request);
        Ticket ticket = ticketService.findEditableTicket(id, actor);
        ticketService.delete(ticket, actor, request);
        return AuthResultDto.success("Ticket deleted");
    }

    private User currentUser(HttpServletRequest request) {
        return currentUserProvider.from(request).orElseThrow();
    }

    private void addCurrentUserToModel(Model model, User user) {
        model.addAttribute("user", user);
        model.addAttribute("canManageAllTickets", isManager(user));
    }

    private boolean isManager(User user) {
        return user.getRole() == Role.MANAGER;
    }

    private TicketResponseDto toResponse(Ticket ticket) {
        return new TicketResponseDto(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getSeverity().name(),
                ticket.getStatus().name(),
                ticket.getOwner().getId(),
                ticket.getOwner().getEmail(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }
}
