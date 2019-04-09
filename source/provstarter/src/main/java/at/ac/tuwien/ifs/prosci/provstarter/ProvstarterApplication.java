package at.ac.tuwien.ifs.prosci.provstarter;

import at.ac.tuwien.ifs.prosci.provstarter.helper.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SpringBootApplication
public class ProvstarterApplication {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	public static void main(String[] args) {
		SpringApplication.run(ProvstarterApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			BufferedReader br = null;
			try {
			Systemmenu systemmenu = (Systemmenu) ctx.getBean("systemmenu");

				LOGGER.info("Start Prosci application.");
				br = new BufferedReader(new InputStreamReader(System.in));
				boolean setupWorkspace=false;
				while (true) {
					String input = br.readLine();
					StatusCode statusCode=null;
					if(input!=null) {
						if (!setupWorkspace) {
							if (!input.startsWith("workspace")&&!input.equals("help")&&!input.equals("show workspaces")) {
								System.out.println("Please set the workspace.");
							} else {
								statusCode=systemmenu.option(input);
							}
						} else {
							systemmenu.option(input);
						}
					}
					if(statusCode!=null&&statusCode.toString().equals(StatusCode.SUCCESS.toString())){
						setupWorkspace=true;
						System.out.println(statusCode);
					}
				}

			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		};

	}


}

