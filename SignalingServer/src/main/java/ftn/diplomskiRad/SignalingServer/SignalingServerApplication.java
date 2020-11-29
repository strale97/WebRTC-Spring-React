package ftn.diplomskiRad.SignalingServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
public class SignalingServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SignalingServerApplication.class, args);
	}
}
