package com.kalixys.amadeus.api.soap;

import com.amadeus.xml._2008._10.ama.profile.AMAProfileCreateRQ;

public record ProfileCreateRequest(AMAProfileCreateRQ payload, SessionContext session) {
}
