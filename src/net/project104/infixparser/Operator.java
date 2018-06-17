package net.project104.infixparser;

import static net.project104.infixparser.Constants.*;

import java.util.ArrayList;

/**
 * Rules from https://docs.python.org/3/reference/expressions.html
 * Operators in the same box [precedence] group left to right 
 * (except for exponentiation, which groups from right to left)
 * @author civyshk
 * @version 20180617
 */
public enum Operator {

	ADDITION("ADD", 2, 2), SUBTRACTION("SUBTRACT", 2, 2),
	MULTIPLICATION("MULTIPLY", 2, 1), DIVISION("DIVIDE", 2, 1), MODULO("MOD", 2, 1),
	EXPONENTIATION("POW", 2, 0),

	SQUARE("SQUARE", 1), SQUAREROOT("SQRT", 1), ROOTYX("ROOT", 2), NEGATIVE("NEG", 1), INVERSION("INVERSION", 1),

	LOG10("LOG10", 1), LOGYX("LOG", 2), LOGN("LOGN", 1), EXPONENTIAL("EXP", 1), FACTORIAL("FACT", 1),

	SINE("SIN", 1), COSINE("COS", 1), TANGENT("TAN", 1),
	ARCSINE("ASIN", 1), ARCCOSINE("ACOS", 1), ARCTANGENT("ATAN", 1),
	SINE_H("SINH", 1), COSINE_H("COSH", 1), TANGENT_H("TANH", 1),
	DEGTORAD("RAD", 1), RADTODEG("DEG", 1),

	FLOOR("FLOOR", 1), ROUND("ROUND", 1), CEIL("CEIL", 1),

	RANDOM("RAND", 0),

	SUMMATION("SUM", ARITY_ALL), SUMMATION_N("SUM", ARITY_N),
	MEAN("AVG", ARITY_ALL), MEAN_N("AVG", ARITY_N),

	CONSTANTPI("PI", ARITY_ZERO_ONE), CONSTANTEULER("E", ARITY_ZERO_ONE), CONSTANTPHI("PHI", ARITY_ZERO_ONE),

	CIRCLE_SURFACE("SFCCIRCLE", 1), TRIANGLE_SURFACE("SFCTRIANGLE", 3),
	HYPOTENUSE_PYTHAGORAS("PYTHAHYPO", 2), LEG_PYTHAGORAS("PYTHALEG", 2),
	QUARATIC_EQUATION("SOLVE", 3);

	public static ArrayList<String> functionNames;
	static {
		functionNames = new ArrayList<>();
		for(Operator op : Operator.values()){
			functionNames.add(op.getFName());
		}
	}

	/**
	 * Text representation of the operation, which the user can
	 * write to access it
	 */
	private final String fName;

	/** 
	 * Lower values of precedence mean a higher binding priority
	 * when grouping expressions. It only applies to binary
	 * operators
	 */
	private final int precedence;
	
	/**
	 * arity is 2 for binary operators but may hold different
	 * values for other operators
	 */
	private final int arity;
	
	/**
	 * @param arity The number of operands that this operator needs
	 */
	Operator(String fName, int arity){
		this(fName, arity, -1);
	}
	
	/**
	 * @param arity The number of operands that this operator needs
	 * @param precedence The highest values mean the lowest binding power
	 */
	Operator(String fName, int arity, int precedence){
		this.fName = fName;
		this.arity = arity;
		this.precedence = precedence;
	}

	public String getFName(){
		return fName;
	}
	
	public int getPrecedence() {
		return precedence;
	}
	
	public int getArity() {
		return arity;
	}
	
	static public boolean anyStarts(String candidate) {
//		return functionNames.stream().anyMatch(s -> s.startsWith(candidate));
		for (String fun : functionNames) {
			if (fun.startsWith(candidate)) {
				return true;
			}
		}
		return false;
	}
	
	static public Operator fromName(String name) throws IllegalArgumentException {
		for(Operator op : Operator.values()){
			if(op.getFName().equals(name)){
				return op;
			}
		}
		throw new IllegalArgumentException("There is no function with that name");
	}
}
