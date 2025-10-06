package com.fighthub.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fighthub.exception.dto.ErrorResponse;
import com.fighthub.utils.ErrorBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.management.BadAttributeValueExpException;
import java.util.Arrays;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioNaoEncontrado(
            UsuarioNaoEncontradoException ex,
            HttpServletRequest request
    ) {
        return ErrorBuilder.build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AlunoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleAlunoNaoEncontrado(
            AlunoNaoEncontradoException ex,
            HttpServletRequest request
    ) {
        return ErrorBuilder.build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleTokenInvalido(
            TokenInvalidoException ex,
            HttpServletRequest request
    ) {
        return ErrorBuilder.build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(TokenExpiradoException.class)
    public ResponseEntity<ErrorResponse> handleTokenInvalido(
            TokenExpiradoException ex,
            HttpServletRequest request
    ) {
        return ErrorBuilder.build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        return ErrorBuilder.build(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos.", request.getRequestURI());
    }

    @ExceptionHandler(EmailNaoEnviadoException.class)
    public ResponseEntity<ErrorResponse> handleEmailNaoEnviadoException(
            EmailNaoEnviadoException ex,
            HttpServletRequest request
    ) {
        return ErrorBuilder.build(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MatriculaInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleMatriculaInvalidaException(
            MatriculaInvalidaException ex,
            HttpServletRequest request
    ) {
        return ErrorBuilder.build(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(CpfExistenteException.class)
    public ResponseEntity<ErrorResponse> handleCpfExistenteException(
            CpfExistenteException ex,
            HttpServletRequest request
    ) {
        return ErrorBuilder.build(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<ErrorResponse> handleValidacaoException(
            ValidacaoException ex,
            HttpServletRequest request
    ) {
        return ErrorBuilder.build(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        return ErrorBuilder.build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleErroInterno(
            Exception ex,
            HttpServletRequest request
    ) {
        return ErrorBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<String> validationError = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        return ErrorBuilder.build(HttpStatus.BAD_REQUEST, "Erro de validação", request.getRequestURI(), validationError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        Throwable causa = ex.getCause();
        if (causa instanceof InvalidFormatException ife && ife.getTargetType().isEnum()) {
            String nomeCampo = ife.getPath().isEmpty() ? "desconhecido" : ife.getPath().get(0).getFieldName();
            String valorInvalido = String.valueOf(ife.getValue());
            List<String> valoresPermitidos = Arrays.stream(ife.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .toList();

            String mensagem = String.format(
                    "Valor inválido para o campo '%s': '%s'. Valores permitidos: %s",
                    nomeCampo,
                    valorInvalido,
                    valoresPermitidos
            );

            return ErrorBuilder.build(HttpStatus.BAD_REQUEST, mensagem, request.getRequestURI());
        }
        return ErrorBuilder.build(
                HttpStatus.BAD_REQUEST,
                "Erro ao ler o corpo da requisição. Verifique o formato dos dados enviados.",
                request.getRequestURI()
        );
    }
}
