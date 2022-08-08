/**
 * This class approximates the result of integral of a provided maximum third degree polynomial.
 * @author Ali Hakan Ozen
 * @since Date: 13.06.2022
 */

import java.util.ArrayList;
import java.util.List;

public class Polynomial {
	
	List<Double> coefficients = new ArrayList<>();
	Double deltaX;

	/**
	 * This constructor sets the coefficients of requested polynomial.
	 * @param a; indicates the highest degree indeterminate, which is third. 
	 * @param b; indicates the second degree indeterminate.
	 * @param c; indicates the first degree indeterminate
	 * @param d; indicates the zeroth degree indeterminate, which is also referred to as constant.
	 */
	public Polynomial(double a, double b, double c, double d) {
		coefficients.add(a); 
		coefficients.add(b);
		coefficients.add(c);
		coefficients.add(d);
	}
	
	/**
	 * Finds the value of polynomial at certain "x" value.
	 * @param x; indicates the indeterminate "x" in the polynomial.
	 * @return value
	 */
	public double valueAt(double x) {
		double value = coefficients.get(0) * Math.pow(x, 3) + coefficients.get(1) * Math.pow(x, 2) + coefficients.get(2) * Math.pow(x, 1) + coefficients.get(3) * Math.pow(x, 0);
		return value;
	}
	
	/**
	 * Sets width of the rectangles in Riemann sum method. Lower it is, more precise the integral shall be.
	 * @param deltaX; mathematical term, meaning width for each rectangle.
	 */
	public void setDeltaX(double deltaX) {
		if (deltaX > 0)
			this.deltaX = deltaX;
		else
			throw new ArithmeticException("deltaX cannot be equal to or lower than zero.");
	}
	
	/**
	 * Finally computes the integral with given parameters from the class and also range. Uses Riemann sum method, thence finds an approximate result.
	 * @param minX; minimum "x" value for polynomial, beginning of the range.
	 * @param maxX; maximum "x" value for polynomial, end of the range.
	 * @return integral
	 */
	public double computeIntegral(double minX, double maxX) {
		if(maxX >= minX) {
		double subintervals = (maxX - minX) / deltaX;
		double integral = 0;
		for (int countSubintervals = 1; countSubintervals <= subintervals; countSubintervals++) {
			double area = deltaX * (valueAt(minX + (deltaX * countSubintervals)));
			integral += area;
		}
		return integral;		
		}
		else 
			throw new ArithmeticException("maxX cannot be lower than minX.");
	}

	/**
	 * Normally, "Polynomial" class has been created to be summoned by other classes, but to initiate an example, "main" method has been implemented.
	 * @param args
	 */
	public static void main(String[] args) {
		Polynomial equation = new Polynomial(0,0,0,3);
		equation.setDeltaX(0.0000001);
		double integralValue = equation.computeIntegral(-6, 6);
		System.out.println("result = " + integralValue);
	}
}

