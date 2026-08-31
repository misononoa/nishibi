package cc.misononoa.nishibi.controller;

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
public class GlobalErrorHandler {

    @HxRetarget("body")
    @HxReswap(HxSwapType.OUTER_HTML)
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleInternalServerError(RuntimeException ex) {
        ex.printStackTrace();
        var rse = new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                Optional.ofNullable(ex.getMessage()).orElse("内部エラーです。ごめんね。"),
                ex);
        return handleErrorResponse(rse);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoResourceFoundError(NoResourceFoundException ex) {
        var rse = new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "ないよ",
                ex);
        return handleErrorResponse(rse);
    }

    private static final String STATUS_FMT = "%03d %s";

    @HxRetarget("html")
    @HxReswap(HxSwapType.OUTER_HTML)
    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView handleErrorResponse(ResponseStatusException ex) {
        final var status = HttpStatus.valueOf(ex.getStatusCode().value());
        final var mav = new ModelAndView("error::errorBody", status);
        final var errorTitle = STATUS_FMT.formatted(
                status.value(),
                status.name().toLowerCase());
        mav.addObject("errorTitle", errorTitle);
        mav.addObject("errorMessage", ex.getReason());
        return mav;
    }

}
