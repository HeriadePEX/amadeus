package com.kalixys.amadeus.api.soap;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "amadeus.soap.auth")
public class SoapAuthProperties {

    @NotBlank
    private String userId;

    @NotBlank
    private String password;

    @NotBlank
    private String officeId;

    private String agentDutyCode = "SU";

    private String posType = "1";

    private String requestorType = "U";

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public String getAgentDutyCode() {
        return agentDutyCode;
    }

    public void setAgentDutyCode(String agentDutyCode) {
        this.agentDutyCode = agentDutyCode;
    }

    public String getPosType() {
        return posType;
    }

    public void setPosType(String posType) {
        this.posType = posType;
    }

    public String getRequestorType() {
        return requestorType;
    }

    public void setRequestorType(String requestorType) {
        this.requestorType = requestorType;
    }
}
