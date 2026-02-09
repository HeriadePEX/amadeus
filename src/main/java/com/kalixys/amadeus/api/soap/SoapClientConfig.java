package com.kalixys.amadeus.api.soap;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;

@Configuration
@EnableConfigurationProperties({SoapAuthProperties.class, SoapEndpointProperties.class})
public class SoapClientConfig {

    @Bean
    public SaajSoapMessageFactory soapMessageFactory() {
        return new SaajSoapMessageFactory();
    }

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(
            com.amadeus.xml._2008._10.ama.profile.ObjectFactory.class,
            com.amadeus.xml.fudreq_01_1_1a.ObjectFactory.class,
            com.amadeus.xml.fudres_01_1_1a.ObjectFactory.class,
            com.amadeus.xml.vlssoq_04_1_1a.ObjectFactory.class,
            com.amadeus.xml.vlssor_04_1_1a.ObjectFactory.class
        );
        return marshaller;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate(SaajSoapMessageFactory messageFactory, Jaxb2Marshaller marshaller) {
        WebServiceTemplate template = new WebServiceTemplate();
        template.setMessageFactory(messageFactory);
        template.setMarshaller(marshaller);
        template.setUnmarshaller(marshaller);
        return template;
    }
}
