package com.kalixys.amadeus.api.soap;

import com.amadeus.xml.fudreq_01_1_1a.ProfileListDeactivatedProfiles;

public record ProfileListDeactivatedRequest(ProfileListDeactivatedProfiles payload, SessionContext session) {
}
