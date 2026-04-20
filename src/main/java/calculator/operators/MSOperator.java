// Clase añadida para representar el operador de memoria Store (MS) en la calculadora

/**
 * @name        MS operator
 * @package     calculator.operators
 * @file        MSOperator.java
 * @description 
 */

package calculator.operators;

public class MSOperator implements UnaryOperator{
    @Override
    public Double execute(Double num) {
        CalculatorMemory.memory = num; // Store number in memory
        return num;
    }
}