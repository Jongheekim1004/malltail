package malltail.common;


import malltail.ShippingApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = { ShippingApplication.class })
public class CucumberSpingConfiguration {
    
}
