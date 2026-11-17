package cr.ac.ucr.if0009.insecurepay;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.secret=ClaveSoloParaPruebasUnitarias0123456789ABCDEF")
class InsecurePayApplicationTests {

    @Test
    void contextLoads() {
        // Valida que el esquema H2, data.sql y el contexto Spring Boot inicien sin errores
    }
}

