package ma.veltra.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponse;

import java.net.URI;
import java.time.OffsetDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ProblemDetail buildProblem(
            HttpStatusCode status,
            String title,
            String code,
            String message,
            HttpServletRequest request
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(status, message);

        problem.setTitle(title);
        problem.setType(URI.create("about:blank"));
        problem.setInstance(URI.create(request.getRequestURI()));

        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("code", code);
        problem.setProperty("message", message);
        problem.setProperty("chemin", request.getRequestURI());

        return problem;
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ProblemDetail handleConflict(
            ResourceAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.CONFLICT,
                "Resource already exists",
                "RESOURCE_ALREADY_EXISTS",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                "RESOURCE_NOT_FOUND",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid request",
                "INVALID_ARGUMENT",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(
            IllegalStateException exception,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.CONFLICT,
                "Business rule violation",
                "INVALID_STATE",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = buildProblem(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "VALIDATION_ERROR",
                "Les données fournies sont invalides",
                request
        );

        problem.setProperty(
                "errors",
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error ->
                                error.getField() + ": "
                                        + error.getDefaultMessage()
                        )
                        .toList()
        );

        return problem;
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ProblemDetail handleForbidden(
            ForbiddenOperationException exception,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.FORBIDDEN,
                "Forbidden operation",
                "FORBIDDEN_OPERATION",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Missing request parameter",
                "MISSING_PARAMETER",
                "Paramètre obligatoire manquant : " + ex.getParameterName(),
                request
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleMethodValidation(
            HandlerMethodValidationException ex,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "VALIDATION_ERROR",
                "Un ou plusieurs paramètres de requête sont invalides",
                request
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = buildProblem(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method not allowed",
                "METHOD_NOT_ALLOWED",
                "Cette méthode HTTP n'est pas autorisée pour cette ressource",
                request
        );

        ResponseEntity.BodyBuilder response =
                ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED);

        String[] supported = ex.getSupportedMethods();

        if (supported != null) {
            response.header(HttpHeaders.ALLOW, String.join(", ", supported));
        }

        return response.body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid request body",
                "INVALID_REQUEST_BODY",
                "Le corps JSON de la requête est absent ou mal formé.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        if (ex instanceof ErrorResponse errorResponse) {
            var status = errorResponse.getStatusCode();

            if (status.is5xxServerError()) {
                log.error("Erreur interne : {}", request.getRequestURI(), ex);
            }

            String message = status.is5xxServerError()
                    ? "Une erreur interne est survenue."
                    : errorResponse.getBody().getDetail();

            if (message == null) {
                message = "Erreur lors du traitement de la requête";
            }

            return ResponseEntity.status(status)
                    .headers(errorResponse.getHeaders())
                    .body(buildProblem(
                            status,
                            status.is5xxServerError()
                                    ? "Internal server error"
                                    : "HTTP error",
                            status.is5xxServerError()
                                    ? "INTERNAL_ERROR"
                                    : "HTTP_ERROR",
                            message,
                            request
                    ));
        }

        log.error("Erreur interne : {}", request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildProblem(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal server error",
                        "INTERNAL_ERROR",
                        "Une erreur interne est survenue.",
                        request
                ));
    }
}
