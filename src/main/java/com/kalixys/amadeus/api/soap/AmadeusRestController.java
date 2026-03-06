package com.kalixys.amadeus.api.soap;

import com.amadeus.xml._2008._10.ama.profile.AMADeleteRS;
import com.amadeus.xml._2008._10.ama.profile.AMAProfileCreateRQ;
import com.amadeus.xml._2008._10.ama.profile.AMAProfileCreateRS;
import com.amadeus.xml._2008._10.ama.profile.AMAProfileReadRQ;
import com.amadeus.xml._2008._10.ama.profile.AMAProfileReadRS;
import com.amadeus.xml._2008._10.ama.profile.AMAUpdateRQ;
import com.amadeus.xml._2008._10.ama.profile.AMAUpdateRS;
import com.amadeus.xml._2008._10.ama.profile.ProfilesType;
import com.amadeus.xml.fudreq_01_1_1a.ProfileListDeactivatedProfiles;
import com.amadeus.xml.fudres_01_1_1a.ProfileListDeactivatedProfilesReply;
import com.amadeus.xml.vlssor_04_1_1a.SecuritySignOutReply;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        AMAProfileCreateRQ payload = requirePayload(request.payload());
        return soapClient.call(ACTION_PROFILE_CREATE, payload, AMAProfileCreateRS.class, request.session());
    }

    @PostMapping(value = "/profiles/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SearchSoapResponse<AMAProfileReadRS> readProfile(
        @RequestBody ProfileReadRequest request,
        @RequestParam(required = false) Integer size
    ) {
        validateSize(size);
        AMAProfileReadRQ payload = requirePayload(request.payload());
        if (size != null) {
            payload.setMaxResponses(BigInteger.valueOf(size));
        }
        SoapResponse<AMAProfileReadRS> response = soapClient.call(ACTION_PROFILE_READ, payload, AMAProfileReadRS.class, request.session());
        return new SearchSoapResponse<>(response.payload(), response.session(), countReadProfiles(response.payload()));
    }

    @PutMapping(value = "/profiles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SoapResponse<AMAUpdateRS> updateProfile(@RequestBody ProfileUpdateRequest request) {
        AMAUpdateRQ payload = requirePayload(request.payload());
        return soapClient.call(ACTION_PROFILE_UPDATE, payload, AMAUpdateRS.class, request.session());
    }

    @DeleteMapping(value = "/profiles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SoapResponse<AMADeleteRS> deleteProfile(@RequestBody ProfileDeleteRequest request) {
        return soapClient.call(ACTION_PROFILE_DELETE, requirePayload(request.payload()), AMADeleteRS.class, request.session());
    }

    @PostMapping(value = "/profiles/deactivated", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SearchSoapResponse<ProfileListDeactivatedProfilesReply> listDeactivatedProfiles(
        @RequestBody ProfileListDeactivatedRequest request,
        @RequestParam(required = false) Integer size
    ) {
        validateSize(size);
        ProfileListDeactivatedProfiles payload = requirePayload(request.payload());
        if (size != null) {
            ensureDeactivatedPageSize(payload, size);
        }
        SoapResponse<ProfileListDeactivatedProfilesReply> response = soapClient.call(ACTION_PROFILE_LIST_DEACTIVATED, payload,
            ProfileListDeactivatedProfilesReply.class, request.session());
        return new SearchSoapResponse<>(response.payload(), response.session(), countDeactivatedProfiles(response.payload()));
    }

    @DeleteMapping(value = "/sessions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SoapResponse<SecuritySignOutReply> signOut(@RequestBody SecuritySignOutRequest request) {
        return soapClient.call(ACTION_SECURITY_SIGN_OUT, requirePayload(request.payload()), SecuritySignOutReply.class, request.session());
    }

    private static int countReadProfiles(AMAProfileReadRS response) {
        ProfilesType profiles = response == null ? null : response.getProfiles();
        if (profiles == null) {
            return 0;
        }
        return profiles.getProfileInfo().size();
    }

    private static int countDeactivatedProfiles(ProfileListDeactivatedProfilesReply response) {
        if (response == null) {
            return 0;
        }
        return response.getFollowUpSection().size();
    }

    private static void ensureDeactivatedPageSize(ProfileListDeactivatedProfiles payload, int size) {
        ProfileListDeactivatedProfiles.InputIndicator indicator = payload.getInputIndicator();
        if (indicator == null) {
            indicator = new ProfileListDeactivatedProfiles.InputIndicator();
            payload.setInputIndicator(indicator);
        }
        indicator.setNumberOfLinesToBeReturned(BigDecimal.valueOf(size));
    }

    private static void validateSize(Integer size) {
        if (size != null && size < 1) {
            throw new IllegalArgumentException("size must be >= 1");
        }
    }

    private static <T> T requirePayload(T payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload is required");
        }
        return payload;
    }
}
