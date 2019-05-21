package at.ac.tuwien.ifs.prosci.filemonitor.filemonitor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FilemonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(FilemonitorApplication.class, args);
	}


	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			Trace trace=(Trace)ctx.getBean("trace");
			trace.run();
		};

	}

}
