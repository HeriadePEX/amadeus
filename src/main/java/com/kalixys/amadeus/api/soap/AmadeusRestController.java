package com.kalixys.amadeus.api.soap;

import com.amadeus.xml._2008._10.ama.profile.AMADeleteRS;
import com.amadeus.xml._2008._10.ama.profile.AMAProfileCreateRS;
import com.amadeus.xml._2008._10.ama.profile.AMAProfileReadRS;
import com.amadeus.xml._2008._10.ama.profile.AMAProfileReadRQ;
import com.amadeus.xml._2008._10.ama.profile.AMAUpdateRS;
import com.amadeus.xml._2008._10.ama.profile.ExternalIDType;
import com.amadeus.xml._2008._10.ama.profile.ProfilesType;
import com.amadeus.xml.fudreq_01_1_1a.ProfileListDeactivatedProfiles;
import com.amadeus.xml.fudres_01_1_1a.ProfileListDeactivatedProfilesReply;
import com.amadeus.xml.vlssor_04_1_1a.SecuritySignOutReply;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
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
    private static final int DEFAULT_PAGE = 1;
    private static final int MAX_SIZE = 150;
    private static final String PAGINATION_HAS_MORE = "Y";
    private static final String PAGINATION_NO_MORE = "N";

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
    public PaginatedSoapResponse<AMAProfileReadRS> readProfile(
        @RequestBody ProfileReadRequest request,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        validatePagination(page, size);
        AMAProfileReadRQ payload = requirePayload(request.payload());
        payload.setMaxResponses(BigInteger.valueOf(size));
        payload.setMoreDataEchoToken(null);
        SessionContext session = request.session();

        SoapResponse<AMAProfileReadRS> response = null;
        for (int currentPage = DEFAULT_PAGE; currentPage <= page; currentPage++) {
            response = soapClient.call(ACTION_PROFILE_READ, payload, AMAProfileReadRS.class, session);
            session = response.session();
            AMAProfileReadRS payloadResponse = response.payload();

            if (currentPage == page) {
                return new PaginatedSoapResponse<>(
                    payloadResponse,
                    response.session(),
                    new PaginationMetadata(
                        page,
                        size,
                        countReadProfiles(payloadResponse),
                        hasMoreReadResults(payloadResponse),
                        computeReadNextCursor(payloadResponse)
                    )
                );
            }

            if (!hasMoreReadResults(payloadResponse) || !advanceReadCursor(payload, payloadResponse)) {
                AMAProfileReadRS emptyPayload = emptyReadPage(payloadResponse);
                return new PaginatedSoapResponse<>(
                    emptyPayload,
                    response.session(),
                    new PaginationMetadata(page, size, 0, false, null)
                );
            }
        }

        throw new IllegalStateException("Unreachable pagination state for profile search");
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
    public PaginatedSoapResponse<ProfileListDeactivatedProfilesReply> listDeactivatedProfiles(
        @RequestBody ProfileListDeactivatedRequest request,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        validatePagination(page, size);
        ProfileListDeactivatedProfiles payload = requirePayload(request.payload());
        ensureDeactivatedPageSize(payload, size);
        SessionContext session = request.session();

        SoapResponse<ProfileListDeactivatedProfilesReply> response = null;
        for (int currentPage = DEFAULT_PAGE; currentPage <= page; currentPage++) {
            response = soapClient.call(ACTION_PROFILE_LIST_DEACTIVATED, payload,
                ProfileListDeactivatedProfilesReply.class, session);
            session = response.session();
            ProfileListDeactivatedProfilesReply payloadResponse = response.payload();

            if (currentPage == page) {
                return new PaginatedSoapResponse<>(
                    payloadResponse,
                    response.session(),
                    new PaginationMetadata(
                        page,
                        size,
                        countDeactivatedProfiles(payloadResponse),
                        hasMoreDeactivatedResults(payloadResponse),
                        computeNextDeactivatedCursor(payloadResponse)
                    )
                );
            }

            if (!hasMoreDeactivatedResults(payloadResponse)) {
                ProfileListDeactivatedProfilesReply emptyPayload = emptyDeactivatedPage(payloadResponse);
                return new PaginatedSoapResponse<>(
                    emptyPayload,
                    response.session(),
                    new PaginationMetadata(page, size, 0, false, null)
                );
            }

            Cursor cursor = extractDeactivatedCursor(payloadResponse);
            if (cursor == null) {
                ProfileListDeactivatedProfilesReply emptyPayload = emptyDeactivatedPage(payloadResponse);
                return new PaginatedSoapResponse<>(
                    emptyPayload,
                    response.session(),
                    new PaginationMetadata(page, size, 0, false, null)
                );
            }
            applyDeactivatedCursor(payload, cursor);
        }

        throw new IllegalStateException("Unreachable pagination state for deactivated profiles");
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

    private static void validatePagination(int page, int size) {
        if (page < DEFAULT_PAGE) {
            throw new IllegalArgumentException("page must be >= 1");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_SIZE);
        }
    }

    private static int countReadProfiles(AMAProfileReadRS response) {
        ProfilesType profiles = response == null ? null : response.getProfiles();
        if (profiles == null) {
            return 0;
        }
        return profiles.getProfileInfo().size();
    }

    private static boolean hasMoreReadResults(AMAProfileReadRS response) {
        return response != null && Boolean.TRUE.equals(response.isMoreIndicator());
    }

    private static boolean advanceReadCursor(AMAProfileReadRQ payload, AMAProfileReadRS response) {
        String token = response.getMoreDataEchoToken();
        if (!isBlank(token)) {
            payload.setMoreDataEchoToken(token);
            return true;
        }

        AMAProfileReadRQ.LastMatchingProfileInfo lastMatchingProfileInfo = extractLastMatchingProfileInfo(response);
        if (lastMatchingProfileInfo == null) {
            return false;
        }
        payload.setMoreDataEchoToken(null);
        payload.setLastMatchingProfileInfo(lastMatchingProfileInfo);
        return true;
    }

    private static String computeReadNextCursor(AMAProfileReadRS response) {
        if (!hasMoreReadResults(response)) {
            return null;
        }
        String token = response.getMoreDataEchoToken();
        if (!isBlank(token)) {
            return token;
        }
        ReadCursor cursor = extractReadCursor(response);
        return cursor == null ? null : cursor.value();
    }

    private static AMAProfileReadRQ.LastMatchingProfileInfo extractLastMatchingProfileInfo(AMAProfileReadRS response) {
        ReadCursor cursor = extractReadCursor(response);
        if (cursor == null) {
            return null;
        }
        return cursor.lastMatchingProfileInfo();
    }

    private static ReadCursor extractReadCursor(AMAProfileReadRS response) {
        ProfilesType profiles = response == null ? null : response.getProfiles();
        if (profiles == null || profiles.getProfileInfo().isEmpty()) {
            return null;
        }
        ProfilesType.ProfileInfo last = profiles.getProfileInfo().get(profiles.getProfileInfo().size() - 1);
        if (last.getProfile() == null) {
            return null;
        }

        AMAProfileReadRQ.LastMatchingProfileInfo lastMatchingProfileInfo = new AMAProfileReadRQ.LastMatchingProfileInfo();
        lastMatchingProfileInfo.setProfile(last.getProfile());
        if (!last.getExternalID().isEmpty()) {
            List<ExternalIDType> externalIds = lastMatchingProfileInfo.getExternalID();
            externalIds.addAll(last.getExternalID());
        }
        if (!last.getUniqueID().isEmpty()) {
            List<AMAProfileReadRQ.LastMatchingProfileInfo.UniqueID> uniqueIds = lastMatchingProfileInfo.getUniqueID();
            for (ProfilesType.ProfileInfo.UniqueID sourceId : last.getUniqueID()) {
                AMAProfileReadRQ.LastMatchingProfileInfo.UniqueID targetId = new AMAProfileReadRQ.LastMatchingProfileInfo.UniqueID();
                targetId.setID(sourceId.getID());
                targetId.setType(sourceId.getType());
                targetId.setInstance(sourceId.getInstance());
                targetId.setIDContext(sourceId.getIDContext());
                targetId.setRPH(sourceId.getRPH());
                targetId.setCompanyName(sourceId.getCompanyName());
                uniqueIds.add(targetId);
            }
        }

        String cursorValue = null;
        if (!last.getUniqueID().isEmpty()) {
            ProfilesType.ProfileInfo.UniqueID first = last.getUniqueID().get(0);
            cursorValue = safeValue(first.getType()) + ":" + safeValue(first.getID()) + ":" + safeValue(first.getInstance());
        }
        return new ReadCursor(lastMatchingProfileInfo, cursorValue);
    }

    private static AMAProfileReadRS emptyReadPage(AMAProfileReadRS response) {
        if (response == null) {
            return new AMAProfileReadRS();
        }
        if (response.getProfiles() != null) {
            response.getProfiles().getProfileInfo().clear();
        }
        response.setMoreIndicator(Boolean.FALSE);
        response.setMoreDataEchoToken(null);
        return response;
    }

    private static void ensureDeactivatedPageSize(ProfileListDeactivatedProfiles payload, int size) {
        ProfileListDeactivatedProfiles.InputIndicator indicator = payload.getInputIndicator();
        if (indicator == null) {
            indicator = new ProfileListDeactivatedProfiles.InputIndicator();
            payload.setInputIndicator(indicator);
        }
        indicator.setNumberOfLinesToBeReturned(BigDecimal.valueOf(size));
    }

    private static int countDeactivatedProfiles(ProfileListDeactivatedProfilesReply response) {
        if (response == null) {
            return 0;
        }
        return response.getFollowUpSection().size();
    }

    private static boolean hasMoreDeactivatedResults(ProfileListDeactivatedProfilesReply response) {
        if (response == null || response.getOutputIndicator() == null
            || response.getOutputIndicator().getOutputGeneralInformation() == null) {
            return false;
        }
        return isTruthy(response.getOutputIndicator().getOutputGeneralInformation().getMoreAnswersExist());
    }

    private static Cursor extractDeactivatedCursor(ProfileListDeactivatedProfilesReply response) {
        List<ProfileListDeactivatedProfilesReply.FollowUpSection> sections = response == null ? null : response.getFollowUpSection();
        if (sections == null || sections.isEmpty()) {
            return null;
        }
        ProfileListDeactivatedProfilesReply.FollowUpSection last = sections.get(sections.size() - 1);
        if (last.getProfileIdentificationSection() == null
            || last.getProfileIdentificationSection().getProfileIdentification() == null) {
            return null;
        }

        String recordLocator = last.getProfileIdentificationSection().getProfileIdentification().getRecordLocator();
        String profileType = last.getProfileIdentificationSection().getProfileIdentification().getProfileType();
        if (isBlank(recordLocator) || isBlank(profileType)) {
            return null;
        }
        return new Cursor(recordLocator, profileType);
    }

    private static void applyDeactivatedCursor(ProfileListDeactivatedProfiles payload, Cursor cursor) {
        ProfileListDeactivatedProfiles.ProfileIdentificationSection section = payload.getProfileIdentificationSection();
        if (section == null) {
            section = new ProfileListDeactivatedProfiles.ProfileIdentificationSection();
            payload.setProfileIdentificationSection(section);
        }

        ProfileListDeactivatedProfiles.ProfileIdentificationSection.ProfileIdentification identification =
            section.getProfileIdentification();
        if (identification == null) {
            identification = new ProfileListDeactivatedProfiles.ProfileIdentificationSection.ProfileIdentification();
            section.setProfileIdentification(identification);
        }
        identification.setRecordLocator(cursor.recordLocator());
        identification.setProfileType(cursor.profileType());
    }

    private static String computeNextDeactivatedCursor(ProfileListDeactivatedProfilesReply response) {
        if (!hasMoreDeactivatedResults(response)) {
            return null;
        }
        Cursor cursor = extractDeactivatedCursor(response);
        if (cursor == null) {
            return null;
        }
        return cursor.recordLocator() + ":" + cursor.profileType();
    }

    private static ProfileListDeactivatedProfilesReply emptyDeactivatedPage(ProfileListDeactivatedProfilesReply response) {
        if (response == null) {
            return new ProfileListDeactivatedProfilesReply();
        }
        response.getFollowUpSection().clear();
        setDeactivatedMoreAnswers(response, PAGINATION_NO_MORE);
        return response;
    }

    private static void setDeactivatedMoreAnswers(ProfileListDeactivatedProfilesReply response, String value) {
        if (response.getOutputIndicator() == null) {
            response.setOutputIndicator(new ProfileListDeactivatedProfilesReply.OutputIndicator());
        }
        ProfileListDeactivatedProfilesReply.OutputIndicator indicator = response.getOutputIndicator();
        if (indicator.getOutputGeneralInformation() == null) {
            indicator.setOutputGeneralInformation(new ProfileListDeactivatedProfilesReply.OutputIndicator.OutputGeneralInformation());
        }
        indicator.getOutputGeneralInformation().setMoreAnswersExist(value);
    }

    private static boolean isTruthy(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim();
        return PAGINATION_HAS_MORE.equalsIgnoreCase(normalized)
            || "TRUE".equalsIgnoreCase(normalized)
            || "1".equals(normalized);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String safeValue(String value) {
        return value == null ? "" : value;
    }

    private record ReadCursor(AMAProfileReadRQ.LastMatchingProfileInfo lastMatchingProfileInfo, String value) {
    }

    private record Cursor(String recordLocator, String profileType) {
    }
}
