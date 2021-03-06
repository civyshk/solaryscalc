/*	Solarys Calc - Console & graphical calculator written in Java
 * 	Copyright 2018 Yeshe Santos García <civyshk@gmail.com>
 *	
 *	This file is part of Solarys Calc
 *	
 *	Solarys Calc is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.project104.solaryscalc;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * RawText is fed with a string which is thought to produce
 * a numeric value. It's the entry point for any text which
 * must be parsed and converted to a numeric result
 * 
 * @author civyshk
 * @version 20180617
 */
public class RawText implements Operand {
	private String content;
	private BigDecimal value;
	private final Calculator calc;
	
	private ArrayList<Operand> operands;
	private ArrayList<Operator> operators;
	private ArrayList<Character> currentTokenList;
	private StringBuilder currentTokenStr;

	/**
	 * Used by parse()
	 * 0 for numbers -1.2e+4
	 * 1 for function names
	 * 2 for operands
	 * 3 for whole parenthesis block
	 * 4 for function arguments inside parenthesis
	 * 5 for finished functions
	 * First iteration works the same as 2
	 */
	private int lastAdded;

	private int openParenthesis;
	private Operation currentFunction;
	
	/**
	 * @param text
	 * @param calc
	 */
	public RawText(String text, Calculator calc) {
		this.content = text.replaceAll("\\s", "");
		this.calc = calc;
		this.value = null;
	}
	
	@Override
	public BigDecimal getValue(){
		if(value == null) {
			parse();
		}
		return value;
	}
	
	/**
	 * Transforms the String content to a list of Operands and Operators
	 */
	private void parse() {
		operands = new ArrayList<>();
		operators = new ArrayList<>();
		currentTokenList = new ArrayList<>();
		currentTokenStr = new StringBuilder();
		lastAdded = 2;
		openParenthesis = 0;
		
		for(int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			this.interpret(c, i);
		}
		
		switch(lastAdded) {
			case 0:
				saveNumber();
				break;
			case 1:
			case 2:
				throw new IllegalArgumentException("Unexpected end of input:\n" + content);
			case 3:
			case 4:
				throw new IllegalArgumentException("Unclosed parenthesis:\n" + content);
			case 5:
				//no action needed
				break;
			default:
				throw new UnsupportedOperationException("Wrongly coded");
		}
		
		if(openParenthesis != 0) {
			throw new IllegalArgumentException("Opening and closing parenthesis don't match:\n" + content);
		}
		
		value = new Expression(operands, operators, calc).getValue();
	}
	
	private void addChar(char c) {	
		currentTokenList.add(Character.toUpperCase(c));
		currentTokenStr.append(Character.toUpperCase(c));
	}
	
	private void clearToken() {
		currentTokenList.clear();
		currentTokenStr.setLength(0);	
	}
	
	private void startNumber(char c) {
		addChar(c);
		lastAdded = 0;
	}
	
	private void saveNumber(){
		operands.add(new Number(currentTokenStr.toString()));
		clearToken();	
	}
	
	private void startFunction(char c) {
		addChar(c);
		lastAdded = 1;
	}
	
	private void saveFunctionName() {
		currentFunction = new Operation(currentTokenStr.toString(), calc);
		currentTokenList.clear();//TODO use clearToken()
		currentTokenStr.setLength(0);
		openParenthesis = 1;
		lastAdded = 4;
	}
	
	private void saveFunctionArgs() {
		currentFunction.setArguments(currentTokenStr.toString());
		operands.add(currentFunction);
		currentFunction = null;
		clearToken();	
		lastAdded = 5;
		
	}
	
	private void saveOperator(char c) {
		switch(c) {
			case '+': operators.add(Operator.ADDITION); break;
			case '-': operators.add(Operator.SUBTRACTION); break;
			case '×':
			case '*': operators.add(Operator.MULTIPLICATION); break;
			case '÷':
			case '/': operators.add(Operator.DIVISION); break;
			case '%': operators.add(Operator.MODULO); break;
			case '^': operators.add(Operator.EXPONENTIATION); break;
			default: throw new UnsupportedOperationException("Wrongly coded");
		}
		lastAdded = 2;
	}
	
	private void startParenthesis() {
		openParenthesis = 1;
		lastAdded = 3;
	}
	
	private void saveParenthesis() throws IllegalStateException {
		if(currentTokenStr.length() == 0) {
			throw new IllegalStateException("Error: Nothing inside a parenthesis");
		}
		
		operands.add(new RawText(currentTokenStr.toString(), calc));
		clearToken();	
		lastAdded = 5;
	}
	
	private void throwUnexpected(char c, int i) {
		unexpected(c, i, null);
	}	
	
	private void unexpected(char c, int i, String info) {
		throw new IllegalArgumentException(String.format(
				"%sUnexpected '%c' at position %d:\n\t%s", info != null ? info + ". " : "", c, i, content));
	}
	
	/**
	 * Used by parse()<br>
	 * A character is legal or not depending on the saved state (lastAdded)
	 * @param c Character to parse
	 * @param i Position of character just for logging purposes
	 */
	private void interpret(char c, int i) {
		switch(lastAdded) {
		case 0:// Number
			if(Character.isDigit(c)) {					
				addChar(c);
			}else if(c == '.') {
				if(currentTokenList.contains('.') || currentTokenList.contains('e') || currentTokenList.contains('E')) {
					unexpected(c, i, "Number wrongly formatted");
				}else {				
					addChar(c);
				}
			}else if(c == 'E' || c == 'e') {
				if(currentTokenList.contains(c)) {
					unexpected(c, i, "Number wrongly formatted");
				}else {	
					addChar(c);
				}					
			}else if(c == '+' || c == '-') {
				if(currentTokenStr.charAt(currentTokenStr.length() - 1) == 'e'
						|| currentTokenStr.charAt(currentTokenStr.length() - 1) == 'E') {	
					addChar(c);
				}else {
					saveNumber();
					saveOperator(c);
				}
			}else if(c == '(' ) {
				unexpected(c, i, "Number ended abruptly");
			}else if(c == '*' || c == '×' || c == '/' || c == '÷' || c == '%' || c == '^') {
				saveNumber();
				saveOperator(c);
			}else if(Character.isLetter(c)) {
				unexpected(c, i, "Number wrongly formatted");
			}else {
				throwUnexpected(c, i);
			}
		break;
		case 1:// Function
			if(Character.isDigit(c) || Character.isLetter(c)) {
				if(Operator.anyStarts(currentTokenStr.toString() + Character.toUpperCase(c))) {
					addChar(c);
				}else {
					throw new IllegalArgumentException("No function with that name at pos " + i + ":\n" + content);
				}
			}else if(c == '(') {
				saveFunctionName();
			}else if(c == '.') {
				unexpected(c, i, "'.' not allowed in function names");
			}else if(c == '+' || c == '-' || c == '*' || c == '×' || c == '/' || c == '÷' || c == '%' || c == '^') {
				unexpected(c, i, "Function needs argument(s)");
			}else {
				throwUnexpected(c, i);
			}
		break;
		case 2:// Operation
			if(Character.isDigit(c) || c == '+' || c == '-') {
				startNumber(c);
			}else if(c == '.') {
				startNumber('0');
				addChar(c);
			}else if(c == '(') {
				startParenthesis();
			}else if(c == '*' || c == '×' || c == '/' || c == '÷' || c == '%' || c == '^') {
				throwUnexpected(c, i);
			}else if(Character.isLetter(c)) {
				if(Operator.anyStarts(String.valueOf(Character.toUpperCase(c)))) {
					startFunction(c);
				}else {
					throw new IllegalArgumentException("No function with that name at pos " + i + ":\n" + content);
				}
			}else {
				throwUnexpected(c, i);
			}
		break;
		case 3:// Expression as an operand
			if(c == ')') {
				openParenthesis--;
				if(openParenthesis == 0) {
					try {
						saveParenthesis();
					}catch(IllegalStateException e) {
						System.out.println(e.getMessage());
						throwUnexpected(c, i);
					}
				}else {
					addChar(c);
				}
			}else {
				addChar(c);
				if(c == '(') {
					openParenthesis++;
				}
			}
		break;
		case 4:// Expression as function arguments
			if(c == ')') {
				openParenthesis--;
				if(openParenthesis == 0) {
					try {
						saveFunctionArgs();
					}catch(IllegalStateException e) {
						System.out.println(e.getMessage());
						throwUnexpected(c, i);
					}
				}else {
					addChar(c);
				}				
			}else {
				addChar(c);
				if(c == '(') {
					openParenthesis++;
				}
			}
		break;
		case 5:// Just finished a parenthesis
			if(c == '+' || c == '-' || c == '*' || c == '×' || c == '/' || c == '÷' || c == '%' || c == '^') {
				saveOperator(c);
			}else {
				throwUnexpected(c, i);
			}
		break;				
		default:
			throw new RuntimeException("Wrongly coded. It shouldn't reach here");				
		}			
	}
}
