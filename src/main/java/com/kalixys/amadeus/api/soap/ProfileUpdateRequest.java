package com.kalixys.amadeus.api.soap;

import com.amadeus.xml._2008._10.ama.profile.AMAUpdateRQ;

public record ProfileUpdateRequest(AMAUpdateRQ payload, SessionContext session) {
}
