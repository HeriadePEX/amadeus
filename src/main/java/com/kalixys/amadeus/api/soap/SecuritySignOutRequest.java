package com.kalixys.amadeus.api.soap;

import com.amadeus.xml.vlssoq_04_1_1a.SecuritySignOut;

public record SecuritySignOutRequest(SecuritySignOut payload, SessionContext session) {
}
