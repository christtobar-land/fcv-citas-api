package co.com.fcv.training.citas.adapter.web;

import co.com.fcv.training.citas.application.AuthFailure;
import co.com.fcv.training.citas.application.DuplicateIdentity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiErrors {
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class, IllegalArgumentException.class, IllegalStateException.class})
    ResponseEntity<ProblemDetail> invalid(Exception ex) {
        String detail = ex.getMessage();
        if (ex instanceof MethodArgumentNotValidException manv && manv.getBindingResult().getFieldError() != null) {
            detail = manv.getBindingResult().getFieldError().getDefaultMessage();
        } else if (detail == null || detail.isBlank() || ex instanceof HttpMessageNotReadableException) {
            detail = "Datos inválidos";
        }
        return problem(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler({DuplicateIdentity.class, DataIntegrityViolationException.class})
    ResponseEntity<ProblemDetail> duplicate(Exception ignored) {
        return problem(HttpStatus.CONFLICT, "Email o documento ya registrado");
    }

    @ExceptionHandler(AuthFailure.class)
    ResponseEntity<ProblemDetail> unauthorized(AuthFailure ignored) {
        return problem(HttpStatus.UNAUTHORIZED, "Credenciales o sesión inválidas");
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(detail);
        return ResponseEntity.status(status).body(body);
    }
}
