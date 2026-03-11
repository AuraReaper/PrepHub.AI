package com.edurag.config;

import com.edurag.dto.ApiDtos;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Global exception handler for clean API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiDtos.ApiError> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiDtos.ApiError.builder()
                        .error("FILE_TOO_LARGE")
                        .message("File size exceeds the maximum limit of 50MB")
                        .status(413)
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiDtos.ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiDtos.ApiError.builder()
                        .error("BAD_REQUEST")
                        .message(ex.getMessage())
                        .status(400)
                        .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiDtos.ApiError> handleRuntime(RuntimeException ex) {
        return ResponseEntity.internalServerError()
                .body(ApiDtos.ApiError.builder()
                        .error("INTERNAL_ERROR")
                        .message(ex.getMessage())
                        .status(500)
                        .build());
    }
}
