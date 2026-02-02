package com.kalixys.amadeus.api.soap;

import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPMessage;
import org.springframework.stereotype.Component;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Component
public class SoapHeaderExtractor {

    private static final String SESSION_NS = "http://xml.amadeus.com/2010/06/Session_v3";

    public SessionContext extract(SoapMessage message) {
        SaajSoapMessage saajSoapMessage = (SaajSoapMessage) message;
        try {
            SOAPMessage saaj = saajSoapMessage.getSaajMessage();
            SOAPHeader header = saaj.getSOAPHeader();
            if (header == null) {
                return null;
            }
            NodeList sessions = header.getElementsByTagNameNS(SESSION_NS, "Session");
            if (sessions.getLength() == 0) {
                return null;
            }
            Element session = (Element) sessions.item(0);
            String status = emptyToNull(session.getAttribute("TransactionStatusCode"));
            String sessionId = childText(session, SESSION_NS, "SessionId");
            Integer sequenceNumber = toInteger(childText(session, SESSION_NS, "SequenceNumber"));
            String securityToken = childText(session, SESSION_NS, "SecurityToken");
            return new SessionContext(sessionId, sequenceNumber, securityToken, status);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String childText(Element parent, String ns, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(ns, localName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return emptyToNull(nodes.item(0).getTextContent());
    }

    private static Integer toInteger(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
