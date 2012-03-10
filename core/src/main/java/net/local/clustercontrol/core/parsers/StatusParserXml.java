package net.local.clustercontrol.core.parsers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import net.local.clustercontrol.core.configuration.Constants;
import net.local.clustercontrol.api.model.xml.JkStatus;

public class StatusParserXml extends IStatusParser {
	
	private static final Logger logger = LoggerFactory.getLogger(StatusParserXml.class);
	
	public StatusParserXml(String body) {
		if(body==null) {
			logger.warn("Supplied status body is null");
			return;
		}
		Schema mySchema;
		SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
	
		String xsdFilename = "/xsd/jkStatus.xsd";
		try {
			InputStream url = getClass().getResourceAsStream(xsdFilename);		    
		    StreamSource ss = new StreamSource(url);
			mySchema = sf.newSchema( ss );
		} catch( SAXException saxe ) {
		    // xsd file not correct
			logger.warn("Failed to find: "+xsdFilename);
		    return;
		}
		try {
			JAXBContext jc = JAXBContext.newInstance(Constants.JAXB_DOMAIN_NAMESPACE);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			unmarshaller.setSchema(mySchema);
			unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
			
			//
			// convert string to byte sequence and create the input stream
			byte currentXMLBytes[] = body.getBytes();
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentXMLBytes); 
			@SuppressWarnings("unchecked")
			JAXBElement<JkStatus> status = (JAXBElement<JkStatus>) unmarshaller.unmarshal(byteArrayInputStream);
			this.jkStatus = status.getValue();
		} catch (UnmarshalException e) {
			logger.debug("UnmarshalException: Could not unmarshal file: "+e.getErrorCode()+": "+e.getMessage());
		} catch (JAXBException e) {
			logger.debug("JAXBException: Could not unmarshal file: "+e.getErrorCode()+": "+e.getMessage());
		}
	}
}