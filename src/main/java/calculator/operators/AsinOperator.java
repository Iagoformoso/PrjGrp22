/**
 * @name        Arcsine of an angle in Degrees
 * @package     calculator.operators
 * @file        ASinOperator.java
 * @description 
 */

package calculator.operators;

public class AsinOperator implements UnaryOperator {
    @Override
    public Double execute(Double num) {
        return Math.toDegrees(Math.asin(num));
    }
}