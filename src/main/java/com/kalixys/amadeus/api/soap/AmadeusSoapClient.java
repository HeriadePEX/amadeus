package com.kalixys.amadeus.api.soap;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;

@Component
public class AmadeusSoapClient {

    private final WebServiceTemplate webServiceTemplate;
    private final Jaxb2Marshaller marshaller;
    private final SoapHeaderBuilder headerBuilder;
    private final SoapHeaderExtractor headerExtractor;
    private final SoapEndpointProperties endpointProperties;

    public AmadeusSoapClient(WebServiceTemplate webServiceTemplate,
                             Jaxb2Marshaller marshaller,
                             SoapHeaderBuilder headerBuilder,
                             SoapHeaderExtractor headerExtractor,
                             SoapEndpointProperties endpointProperties) {
        this.webServiceTemplate = webServiceTemplate;
        this.marshaller = marshaller;
        this.headerBuilder = headerBuilder;
        this.headerExtractor = headerExtractor;
        this.endpointProperties = endpointProperties;
    }

    public <T> SoapResponse<T> call(String action, Object request, Class<T> responseType, SessionContext session) {
        WebServiceMessageCallback callback = message -> {
            SoapMessage soapMessage = (SoapMessage) message;
            soapMessage.setSoapAction(action);
            headerBuilder.apply(soapMessage, action, endpointProperties.getEndpoint(), session);
            marshaller.marshal(request, soapMessage.getPayloadResult());
        };

        return webServiceTemplate.sendAndReceive(endpointProperties.getEndpoint(), callback, message -> {
            SoapMessage soapMessage = (SoapMessage) message;
            Object payload = marshaller.unmarshal(soapMessage.getPayloadSource());
            SessionContext responseSession = headerExtractor.extract(soapMessage);
            return new SoapResponse<>(responseType.cast(payload), responseSession);
        });
    }
}
