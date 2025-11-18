package cc.misononoa.nishibi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class NishibiBbsApplication {

	public static void main(String[] args) {
		SpringApplication.run(NishibiBbsApplication.class, args);
	}

}
