package com.kalixys.amadeus.api.soap;

public record SessionContext(
    String sessionId,
    Integer sequenceNumber,
    String securityToken,
    String transactionStatusCode
) {
}
