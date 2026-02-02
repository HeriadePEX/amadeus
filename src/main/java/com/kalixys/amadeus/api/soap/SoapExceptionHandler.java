package com.kalixys.amadeus.api.soap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.oxm.MarshallingFailureException;
import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.soap.client.SoapFaultClientException;

@RestControllerAdvice
public class SoapExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<SoapErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new SoapErrorResponse("invalid_request", ex.getMessage()));
    }

    @ExceptionHandler(SoapFaultClientException.class)
    public ResponseEntity<SoapErrorResponse> handleSoapFault(SoapFaultClientException ex) {
        String faultString = ex.getFaultStringOrReason();
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(new SoapErrorResponse("soap_fault", faultString));
    }

    @ExceptionHandler(WebServiceIOException.class)
    public ResponseEntity<SoapErrorResponse> handleTransport(WebServiceIOException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(new SoapErrorResponse("soap_transport_error", ex.getMessage()));
    }

    @ExceptionHandler({MarshallingFailureException.class, UnmarshallingFailureException.class})
    public ResponseEntity<SoapErrorResponse> handleMapping(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new SoapErrorResponse("soap_mapping_error", ex.getMessage()));
    }
}
