package com.kalixys.amadeus.api.soap;

public record PaginatedSoapResponse<T>(T payload, SessionContext session, PaginationMetadata pagination) {
}
