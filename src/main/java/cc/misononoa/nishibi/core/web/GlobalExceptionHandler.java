package cc.misononoa.nishibi.core.web;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxReswap;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRetarget;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxSwapType;

@ControllerAdvice
public class GlobalExceptionHandler {

    @HxRetarget("body")
    @HxReswap(HxSwapType.OUTER_HTML)
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleInternalServerError(RuntimeException ex) {
        ex.printStackTrace();
        return buildErrorMav(
                HttpStatus.INTERNAL_SERVER_ERROR,
                Optional.ofNullable(ex.getMessage()).orElse("内部エラーです"),
                false);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoResourceFoundError(NoResourceFoundException ex) {
        return buildErrorMav(HttpStatus.NOT_FOUND, "ないよ", true);
    }

    @HxRetarget("body")
    @HxReswap(HxSwapType.OUTER_HTML)
    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView handleErrorResponse(ResponseStatusException ex) {
        return buildErrorMav(
                HttpStatus.valueOf(ex.getStatusCode().value()),
                ex.getReason(),
                false);
    }

    private ModelAndView buildErrorMav(HttpStatus status, String msg, boolean wholeHtml) {
        final var mav = new ModelAndView(wholeHtml ? "error" : "error::errorBody", status);
        final var errorTitle = "%03d %s".formatted(
                status.value(),
                status.name().toLowerCase());
        mav.addObject("errorTitle", errorTitle);
        mav.addObject("errorMessage", msg);
        return mav;
    }

}
