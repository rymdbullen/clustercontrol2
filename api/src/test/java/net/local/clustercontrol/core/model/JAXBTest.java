package net.local.clustercontrol.core.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import net.local.clustercontrol.api.model.JkBalancer;
import net.local.clustercontrol.api.model.JkBalancers;
import net.local.clustercontrol.api.model.JkMap;
import net.local.clustercontrol.api.model.JkMember;
import net.local.clustercontrol.api.model.JkResult;
import net.local.clustercontrol.api.model.JkServer;
import net.local.clustercontrol.api.model.JkSoftware;
import net.local.clustercontrol.api.model.JkStatus;
import net.local.clustercontrol.api.model.ObjectFactory;

import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class JAXBTest extends TestCase {
	//private static Log logger = LogFactory.getLog(JAXBTest.class);
//	private static final Logger logger = LoggerFactory.getLogger(JAXBTest.class);
	/**
	 * 
	 * @throws JAXBException
	 * @throws FileNotFoundException 
	 * @throws URISyntaxException 
	 */
	@SuppressWarnings("unchecked")
	public void testUnmarshallStatus() throws JAXBException, FileNotFoundException, URISyntaxException {
		JAXBContext jc = JAXBContext.newInstance("net.local.clustercontrol.api.model");
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		
		Schema mySchema;
		SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
		try {
			URL url = getClass().getResource("/xsd/jkStatus.xsd");
		    File schemaFile = new File(url.toURI());
			mySchema = sf.newSchema( schemaFile );
		} catch( SAXException saxe ){
		    // ...(error handling)
		    mySchema = null;
		}
		
		unmarshaller.setSchema(mySchema);
		unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
		//
		// Open file
		final FileInputStream fis = new FileInputStream("src/test/resources/status.xml");

		JAXBElement<JkStatus> jkActionStatus = (JAXBElement<JkStatus>) unmarshaller.unmarshal(fis);
		JkStatus result = jkActionStatus.getValue();
		assertEquals("Balancers.getCount()", new Integer(1), result.getBalancers().getCount());
		
		//logger.debug("Balancers.getCount()="+result.getBalancers().getCount());
		System.out.println("Balancers.getCount()="+result.getBalancers().getCount());
	}
	/**
	 * 
	 * @throws JAXBException
	 * @throws FileNotFoundException 
	 * @throws URISyntaxException 
	 */
	@SuppressWarnings("unchecked")
	public void testUnmarshallActionStatus() throws JAXBException, FileNotFoundException, URISyntaxException {
		JAXBContext jc = JAXBContext.newInstance("net.local.clustercontrol.api.model");
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		
		Schema mySchema;
		SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
		try {
			URL url = getClass().getResource("/xsd/jkStatus.xsd");
		    File schemaFile = new File(url.toURI());
			mySchema = sf.newSchema( schemaFile );
		} catch( SAXException saxe ){
		    // ...(error handling)
		    mySchema = null;
		}
		
		unmarshaller.setSchema(mySchema);
		unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
		//
		// Open file
		final FileInputStream fis = new FileInputStream("src/test/resources/actionStatus.xml");

		JAXBElement<JkStatus> jkActionStatus = (JAXBElement<JkStatus>) unmarshaller.unmarshal(fis);
		JkStatus result = jkActionStatus.getValue();
		//logger.debug("Result: "+result.getResult().getType()+" "+result.getResult().getMessage());
		System.out.println("Result: "+result.getResult().getType()+" "+result.getResult().getMessage());
	}
	/**
	 * 
	 * @throws JAXBException
	 */
	public void testMarshall() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance("net.local.clustercontrol.api.model");
		ObjectFactory factory = new ObjectFactory();
		JkStatus status = factory.createJkStatus();
		JkServer server = factory.createJkServer();
		server.setName("localhost");
		status.setServer(server);
		JkResult result = new JkResult();
		result.setMessage("message");
		result.setType("type");
		status.setResult(result);
		JkBalancers balancers = factory.createJkBalancers();
		JkBalancer balancer = factory.createJkBalancer();
		JkMember jkMember = new JkMember(); 
		balancer.getMember().add(jkMember);
		JkMap map = new JkMap();
		balancer.getMap().add(map);
		JkSoftware software = factory.createJkSoftware();
		balancers.setBalancer(balancer);
		
		status.setBalancers(balancers);
		status.setSoftware(software);
		
		Schema mySchema;
		SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
		try {
			URL url = getClass().getResource("/xsd/jkStatus.xsd");
		    File schemaFile = new File(url.toURI());
			mySchema = sf.newSchema( schemaFile );
		} catch( SAXException saxe ){
		    // could not read xsd, set null schema
		    mySchema = null;
		} catch (URISyntaxException e) {
			// could not find xsd, set null schema
			mySchema = null;
		}
		JAXBElement<JkStatus> element = factory.createStatus(status);
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.setSchema(mySchema);
		m.marshal(element, System.out);
	}
}