package com.kalixys.amadeus.api.soap;

import com.amadeus.xml._2008._10.ama.profile.AMAProfileReadRQ;

public record ProfileReadRequest(AMAProfileReadRQ payload, SessionContext session) {
}
