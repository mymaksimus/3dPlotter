package de.mrotmann.facharbeitogl.functionparser.seperations;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import de.mrotmann.facharbeitogl.functionparser.seperations.parts.FunctionPart;
import de.mrotmann.facharbeitogl.functionparser.seperations.parts.Variable;
import de.mrotmann.facharbeitogl.functionparser.seperations.parts.Operator;
import de.mrotmann.facharbeitogl.functionparser.seperations.parts.SingleNumber;

public class FunctionPartParser {
	
	private static Predicate<String> isSingleNumber = createPredicate("[0-9.]");
	private static Predicate<String> isOperator = createPredicate("[-+*/\\^]");
	private static Predicate<String> isVariable = createPredicate("[a-z]");
	
	public static Class<? extends FunctionPart<?>> parseFunctionChar(char c){
		return parseFunctionChar(String.valueOf(c));
	}
	
	private static Class<? extends FunctionPart<?>> parseFunctionChar(String charString){
		if(isSingleNumber.test(charString)) return SingleNumber.class;
		if(isOperator.test(charString)) return Operator.class;
		if(isVariable.test(charString)) return Variable.class;
		return null;
	}
	
	private static Predicate<String> createPredicate(String pattern){
		return Pattern.compile(pattern).asPredicate();
	}
}
