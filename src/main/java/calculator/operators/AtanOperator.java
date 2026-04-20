/**
 * @name        Arctangent of an angle in Degrees
 * @package     calculator.operators
 * @file        AtanOperator.java
 * @description 
 */

package calculator.operators;

public class AtanOperator implements UnaryOperator {
    @Override
    public Double execute(Double num) {
        return Math.toDegrees(Math.atan(num));
    }
}