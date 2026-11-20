package fr.diginamic.projet_spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Charge les composants Spring dans un environnement de test, sans ouvrir de port.
@SpringBootTest
class ProjetSpringApplicationTests {

	@Test
	// Le test réussit si Spring arrive à créer tous les composants.
	void contextLoads() {
	}

}
