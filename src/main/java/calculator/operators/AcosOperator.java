/**
 * @name        Arccosine of an angle in Degrees
 * @package     calculator.operators
 * @file        AcosOperator.java
 * @description 
 */

package calculator.operators;

public class AcosOperator implements UnaryOperator {
    @Override
    public Double execute(Double num) {
        return Math.toDegrees(Math.acos(num));
    }
}