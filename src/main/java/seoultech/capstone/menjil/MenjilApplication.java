package seoultech.capstone.menjil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MenjilApplication {

    public static void main(String[] args) {
        SpringApplication.run(MenjilApplication.class, args);
    }

}
