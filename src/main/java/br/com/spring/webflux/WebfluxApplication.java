package br.com.spring.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class WebfluxApplication {

    static {
        BlockHound.install(builder ->
                builder.allowBlockingCallsInside("java.util.UUID", "randomUUID"));
    }

    public static void main(String[] args) {
        SpringApplication.run(WebfluxApplication.class, args);
    }

}
