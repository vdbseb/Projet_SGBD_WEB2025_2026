package be.angularpadelclub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "be.angularpadelclub")
public class PadelBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PadelBackApplication.class, args);
    }
}