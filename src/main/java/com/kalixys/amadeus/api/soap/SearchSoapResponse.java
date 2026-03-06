package com.kalixys.amadeus.api.soap;

public record SearchSoapResponse<T>(T payload, SessionContext session, int resultCount) {
}
