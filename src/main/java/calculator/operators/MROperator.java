// Clase añadida para representar el operador de memoria Recall (MR) en la calculadora

/**
 * @name        MR operator
 * @package     calculator.operators
 * @file        MROperator.java
 * @description 
 */

package calculator.operators;

public class MROperator implements UnaryOperator{
    @Override
    public Double execute(Double num) {
        if(num != null){
            return CalculatorMemory.memory;
        }
        return null;
    }
}
