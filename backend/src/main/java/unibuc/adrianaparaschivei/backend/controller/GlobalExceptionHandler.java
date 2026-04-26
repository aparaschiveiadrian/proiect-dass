package unibuc.adrianaparaschivei.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import unibuc.adrianaparaschivei.backend.exceptions.AccessForbiddenException;
import unibuc.adrianaparaschivei.backend.exceptions.BadRequestException;
import unibuc.adrianaparaschivei.backend.exceptions.ResourceNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessForbiddenException.class)
    public String handleAccessDenied(AccessForbiddenException exception, Model model) {
        logger.warn("Access denied", exception);
        model.addAttribute("message", "You are not allowed to access this resource.");
        return "error";
    }

    @ExceptionHandler(BadRequestException.class)
    public String handleBadInput(BadRequestException exception, Model model) {
        logger.warn("Invalid request input", exception);
        model.addAttribute("message", "The request could not be processed.");
        return "error";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException exception, Model model) {
        logger.warn("Resource not found", exception);
        model.addAttribute("message", "The requested resource was not found.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpectedError(Exception exception, Model model) {
        logger.error("Unexpected application error", exception);
        model.addAttribute("message", "Something went wrong. Please try again later.");
        return "error";
    }
}
