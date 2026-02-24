package cc.misononoa.nishibi.web.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalErrorHandlingAdvice {

    @ExceptionHandler(RuntimeException.class)
    public String handleInternalServerError(RuntimeException ex, Model model) {
        var rse = new ResponseStatusException(
                HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                Optional.ofNullable(ex.getMessage()).orElse("内部エラーです。ごめんね。"),
                ex);
        return handleErrorResponse(rse, model);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundError(NoResourceFoundException ex, Model model) {
        var rse = new ResponseStatusException(
                HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()),
                "ないよ",
                ex);
        return handleErrorResponse(rse, model);
    }

    private static final String STATUS_FMT = "%03d %s";

    @ExceptionHandler(ResponseStatusException.class)
    public String handleErrorResponse(ResponseStatusException ex, Model model) {
        final var status = HttpStatus.valueOf(ex.getStatusCode().value());
        final var errorTitle = STATUS_FMT.formatted(
                status.value(),
                status.name().toLowerCase());
        model.addAttribute("errorTitle", errorTitle);
        model.addAttribute("errorMessage", ex.getReason());
        return "error";
    }

}
