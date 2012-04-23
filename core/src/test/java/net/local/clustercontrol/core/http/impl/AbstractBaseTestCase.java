package net.local.clustercontrol.core.http.impl;

import org.apache.log4j.Logger;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Test classes can extend this manager based on a spring context.
 * This test class can be moved to the test tree.
 * 
 * @author jstenvall
 * @since 1.0
 */
@ContextConfiguration(locations = {
		"classpath:applicationContext.xml"
})
public abstract class AbstractBaseTestCase extends AbstractJUnit4SpringContextTests {
		
    /**
     * A simple logger
     */
    protected final Logger log = Logger.getLogger(AbstractBaseTestCase.class);
    /**
     * The resourceBundle
     */
    protected ResourceBundle rb;

    /**
     * Default constructor will set the ResourceBundle if needed.
     */
    public AbstractBaseTestCase() {
        // Since a ResourceBundle is not required for each class, just
        // do a simple check to see if one exists
        String className = this.getClass().getName();

        try {
            rb = ResourceBundle.getBundle(className);
        } catch (MissingResourceException mre) {
            // log.warn("No resource bundle found for: " + className);
        }
    }
}
