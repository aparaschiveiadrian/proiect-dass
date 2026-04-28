package unibuc.adrianaparaschivei.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import unibuc.adrianaparaschivei.backend.dto.ApiErrorDto;
import unibuc.adrianaparaschivei.backend.exceptions.AccessForbiddenException;
import unibuc.adrianaparaschivei.backend.exceptions.BadRequestException;
import unibuc.adrianaparaschivei.backend.exceptions.ResourceNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessForbiddenException.class)
    public Object handleAccessDenied(AccessForbiddenException exception, HttpServletRequest request, Model model) {
        logger.warn("Access denied", exception);
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorDto(false, "You are not allowed to access this resource."));
        }
        model.addAttribute("message", "You are not allowed to access this resource.");
        return "error";
    }

    @ExceptionHandler(BadRequestException.class)
    public Object handleBadInput(BadRequestException exception, HttpServletRequest request, Model model) {
        logger.warn("Invalid request input", exception);
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorDto(false, "The request could not be processed."));
        }
        model.addAttribute("message", "The request could not be processed.");
        return "error";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleNotFound(ResourceNotFoundException exception, HttpServletRequest request, Model model) {
        logger.warn("Resource not found", exception);
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiErrorDto(false, "The requested resource was not found."));
        }
        model.addAttribute("message", "The requested resource was not found.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public Object handleUnexpectedError(Exception exception, HttpServletRequest request, Model model) {
        logger.error("Unexpected application error", exception);
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorDto(false, "Something went wrong. Please try again later."));
        }
        model.addAttribute("message", "Something went wrong. Please try again later.");
        return "error";
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api");
    }
}
