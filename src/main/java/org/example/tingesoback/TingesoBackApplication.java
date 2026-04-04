package org.example.tingesoback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TingesoBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(TingesoBackApplication.class, args);
    }

}
