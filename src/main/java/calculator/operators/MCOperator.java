// Clase añadida para representar el operador de memoria Clear (MC) en la calculadora

/**
 * @name        MC operator
 * @package     calculator.operators
 * @file        MCOperator.java
 * @description 
 */

package calculator.operators;

public class MCOperator implements UnaryOperator{
    @Override
    public Double execute(Double num) {
        CalculatorMemory.memory = null; // Clear memory
        return null;
    }
}
