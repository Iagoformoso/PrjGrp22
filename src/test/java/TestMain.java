import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class TestMain {

    @Test
    public void testMainOutput() {
        // 1. Preparamos para capturar la consola
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // 2. Ejecutamos el main
        Main.main(new String[]{});

        // 3. Verificamos los resultados
        String output = outContent.toString();
        
        assertTrue(output.contains("Hola"), "Debería imprimir 'Hola'");
        assertTrue(output.contains("Valor de a: 4"), "El valor de 'a' debería ser 4");

        // 4. Restauramos la consola original
        System.setOut(originalOut);
    }
    
}