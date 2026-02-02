package com.kalixys.amadeus.api.soap;

import com.amadeus.xml._2008._10.ama.profile.AMADeleteRS;
import com.amadeus.xml._2008._10.ama.profile.AMAProfileCreateRS;
import com.amadeus.xml._2008._10.ama.profile.AMAProfileReadRS;
import com.amadeus.xml._2008._10.ama.profile.AMAUpdateRS;
import com.amadeus.xml.fudres_01_1_1a.ProfileListDeactivatedProfilesReply;
import com.amadeus.xml.vlssor_04_1_1a.SecuritySignOutReply;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AmadeusRestController {

    private static final String ACTION_PROFILE_CREATE = "http://webservices.amadeus.com/Profile_CreateProfile_12.2";
    private static final String ACTION_PROFILE_READ = "http://webservices.amadeus.com/Profile_ReadProfile_12.2";
    private static final String ACTION_PROFILE_UPDATE = "http://webservices.amadeus.com/Profile_UpdateProfile_12.2";
    private static final String ACTION_PROFILE_DELETE = "http://webservices.amadeus.com/Profile_DeleteProfile_12.2";
    private static final String ACTION_PROFILE_LIST_DEACTIVATED = "http://webservices.amadeus.com/FUDREQ_01_1_1A";
    private static final String ACTION_SECURITY_SIGN_OUT = "http://webservices.amadeus.com/VLSSOQ_04_1_1A";

    private final AmadeusSoapClient soapClient;

    public AmadeusRestController(AmadeusSoapClient soapClient) {
        this.soapClient = soapClient;
    }

    @PostMapping(value = "/profiles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SoapResponse<AMAProfileCreateRS> createProfile(@RequestBody ProfileCreateRequest request) {
        return soapClient.call(ACTION_PROFILE_CREATE, requirePayload(request.payload()), AMAProfileCreateRS.class, request.session());
    }

    @PostMapping(value = "/profiles/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SoapResponse<AMAProfileReadRS> readProfile(@RequestBody ProfileReadRequest request) {
        return soapClient.call(ACTION_PROFILE_READ, requirePayload(request.payload()), AMAProfileReadRS.class, request.session());
    }

    @PutMapping(value = "/profiles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SoapResponse<AMAUpdateRS> updateProfile(@RequestBody ProfileUpdateRequest request) {
        return soapClient.call(ACTION_PROFILE_UPDATE, requirePayload(request.payload()), AMAUpdateRS.class, request.session());
    }

    @DeleteMapping(value = "/profiles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SoapResponse<AMADeleteRS> deleteProfile(@RequestBody ProfileDeleteRequest request) {
        return soapClient.call(ACTION_PROFILE_DELETE, requirePayload(request.payload()), AMADeleteRS.class, request.session());
    }

    @PostMapping(value = "/profiles/deactivated", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SoapResponse<ProfileListDeactivatedProfilesReply> listDeactivatedProfiles(@RequestBody ProfileListDeactivatedRequest request) {
        return soapClient.call(ACTION_PROFILE_LIST_DEACTIVATED, requirePayload(request.payload()),
            ProfileListDeactivatedProfilesReply.class, request.session());
    }

    @DeleteMapping(value = "/sessions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SoapResponse<SecuritySignOutReply> signOut(@RequestBody SecuritySignOutRequest request) {
        return soapClient.call(ACTION_SECURITY_SIGN_OUT, requirePayload(request.payload()), SecuritySignOutReply.class, request.session());
    }

    private static <T> T requirePayload(T payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload is required");
        }
        return payload;
    }
}
