package com.kalixys.amadeus.api.soap;

import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPMessage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

@Component
public class SoapHeaderBuilder {

    private static final String WSA_NS = "http://www.w3.org/2005/08/addressing";
    private static final String WSSE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    private static final String WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    private static final String SESSION_NS = "http://xml.amadeus.com/2010/06/Session_v3";
    private static final String AMA_SECURITY_NS = "http://xml.amadeus.com/2010/06/Security_v1";

    private final SoapAuthProperties authProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public SoapHeaderBuilder(SoapAuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public void apply(SoapMessage message, String action, String endpoint, SessionContext session) {
        SaajSoapMessage saajSoapMessage = (SaajSoapMessage) message;
        try {
            SOAPMessage saaj = saajSoapMessage.getSaajMessage();
            SOAPEnvelope envelope = saaj.getSOAPPart().getEnvelope();
            SOAPHeader header = envelope.getHeader();
            if (header == null) {
                header = envelope.addHeader();
            }
            addWsAddressing(header, action, endpoint);
            if (session != null) {
                addSessionHeader(header, session);
                return;
            }
            addWsSecurity(header);
            addAmadeusSecurity(header);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to build SOAP headers", ex);
        }
    }

    private void addWsAddressing(SOAPHeader header, String action, String endpoint) throws Exception {
        SOAPElement messageId = header.addChildElement("MessageID", "wsa", WSA_NS);
        messageId.addTextNode("urn:uuid:" + UUID.randomUUID());
        SOAPElement actionElement = header.addChildElement("Action", "wsa", WSA_NS);
        actionElement.addTextNode(action);
        SOAPElement toElement = header.addChildElement("To", "wsa", WSA_NS);
        toElement.addTextNode(endpoint);
    }

    private void addWsSecurity(SOAPHeader header) throws Exception {
        SOAPElement security = header.addChildElement("Security", "oas", WSSE_NS);
        SOAPElement usernameToken = security.addChildElement("UsernameToken", "oas");
        usernameToken.addAttribute(envelopedName("Id", "oas1", WSU_NS), "UsernameToken-1");

        SOAPElement username = usernameToken.addChildElement("Username", "oas");
        username.addTextNode(authProperties.getUserId());

        String created = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        byte[] nonceBytes = new byte[16];
        secureRandom.nextBytes(nonceBytes);
        String nonceBase64 = Base64.getEncoder().encodeToString(nonceBytes);

        SOAPElement nonce = usernameToken.addChildElement("Nonce", "oas");
        nonce.addAttribute(envelopedName("EncodingType", null, null),
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
        nonce.addTextNode(nonceBase64);

        String passwordDigest = buildPasswordDigest(nonceBytes, created, authProperties.getPassword());
        SOAPElement password = usernameToken.addChildElement("Password", "oas");
        password.addAttribute(envelopedName("Type", null, null),
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest");
        password.addTextNode(passwordDigest);

        SOAPElement createdElement = usernameToken.addChildElement("Created", "oas1", WSU_NS);
        createdElement.addTextNode(created);
    }

    private void addAmadeusSecurity(SOAPHeader header) throws Exception {
        SOAPElement amaSecurity = header.addChildElement("AMA_SecurityHostedUser", "amasec", AMA_SECURITY_NS);
        SOAPElement userId = amaSecurity.addChildElement("UserID", "amasec");

        if (authProperties.getAgentDutyCode() != null && !authProperties.getAgentDutyCode().isBlank()) {
            userId.setAttribute("AgentDutyCode", authProperties.getAgentDutyCode());
        }
        userId.setAttribute("POS_Type", authProperties.getPosType());
        userId.setAttribute("PseudoCityCode", authProperties.getOfficeId());
        userId.setAttribute("RequestorType", authProperties.getRequestorType());
    }

    private void addSessionHeader(SOAPHeader header, SessionContext session) throws Exception {
        SOAPElement sessionElement = header.addChildElement("Session", "awsse", SESSION_NS);
        if (session.transactionStatusCode() != null && !session.transactionStatusCode().isBlank()) {
            sessionElement.setAttribute("TransactionStatusCode", session.transactionStatusCode());
        }
        if (session.sessionId() != null) {
            SOAPElement sessionId = sessionElement.addChildElement("SessionId", "awsse");
            sessionId.addTextNode(session.sessionId());
        }
        if (session.sequenceNumber() != null) {
            SOAPElement sequenceNumber = sessionElement.addChildElement("SequenceNumber", "awsse");
            sequenceNumber.addTextNode(session.sequenceNumber().toString());
        }
        if (session.securityToken() != null) {
            SOAPElement securityToken = sessionElement.addChildElement("SecurityToken", "awsse");
            securityToken.addTextNode(session.securityToken());
        }
    }

    private static jakarta.xml.soap.Name envelopedName(String localName, String prefix, String ns) throws Exception {
        return jakarta.xml.soap.SOAPFactory.newInstance().createName(localName, prefix, ns);
    }

    private static String buildPasswordDigest(byte[] nonce, String created, String clearPassword) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] passwordSha1 = sha1.digest(clearPassword.getBytes(StandardCharsets.UTF_8));
        byte[] createdBytes = created.getBytes(StandardCharsets.UTF_8);
        byte[] combined = new byte[nonce.length + createdBytes.length + passwordSha1.length];
        System.arraycopy(nonce, 0, combined, 0, nonce.length);
        System.arraycopy(createdBytes, 0, combined, nonce.length, createdBytes.length);
        System.arraycopy(passwordSha1, 0, combined, nonce.length + createdBytes.length, passwordSha1.length);
        byte[] digest = sha1.digest(combined);
        return Base64.getEncoder().encodeToString(digest);
    }
}
