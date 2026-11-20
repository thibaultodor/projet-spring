package fr.diginamic.projet_spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Active Spring Boot et la détection des composants du projet.
@SpringBootApplication
public class ProjetSpringApplication {

	public static void main(String[] args) {
		// Démarre l'application et le serveur web.
		SpringApplication.run(ProjetSpringApplication.class, args);
	}

}
