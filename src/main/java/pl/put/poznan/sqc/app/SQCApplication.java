package pl.put.poznan.sqc.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "pl.put.poznan.sqc")
public class SQCApplication {

    public static void main(String[] args) {
        SpringApplication.run(SQCApplication.class, args);
    }
}
