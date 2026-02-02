package com.kalixys.amadeus.api.soap;

public record SoapResponse<T>(T payload, SessionContext session) {
}
