package at.yawk.catdb.web;

import java.io.IOException;
import java.util.NoSuchElementException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author yawkat
 */
@ControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public void handleException(HttpServletResponse hsr, IllegalStateException e)
            throws IOException {
        hsr.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
    }
}
