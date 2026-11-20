package fr.diginamic.projet_spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Ce test utilise H2 : il ne dépend pas de XAMPP et ne touche pas la base recensement.
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:contexte;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ProjetSpringApplicationTests {

    @Test
    void contextLoads() {
        // Le test réussit si Spring crée les composants et les tables de test.
    }
}
