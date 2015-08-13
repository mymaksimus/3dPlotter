package de.mrotmann.facharbeitogl.functionparser;

import java.util.HashMap;

import de.mrotmann.facharbeitogl.functionparser.seperations.BraceElementSeperation;

public class FunctionParser {
	
	private BraceElementSeperation rootBrace;
	
	public FunctionParser(String functionString){
		parse(functionString);
	}
	
	private void parse(String functionString){
		rootBrace = new BraceElementSeperation();
		BraceElementSeperation currentBrace = rootBrace;
		char contentChars[] = functionString.toCharArray();
		for(char c : contentChars){
			if(c == '('){
				BraceElementSeperation newBraceElementSeperation = new BraceElementSeperation();
				newBraceElementSeperation.setParent(currentBrace);
				currentBrace.newBraceElementRequested();
				currentBrace.addSubBraceSeperation(newBraceElementSeperation);
				currentBrace = newBraceElementSeperation;
			}
			else if(c == ')'){
				currentBrace.endBuild();
				currentBrace = currentBrace.getParent();
			}
			else {
				//normal parsing
				currentBrace.parseChar(c);
			}
		}
		rootBrace.endBuild();
		System.out.println("final result: " + rootBrace);
	}
	
	public double evaluate(HashMap<Character, Double> variableMap){
		return rootBrace.evaluate(variableMap);
	}
	
	public static void main(String[] args){
		HashMap<Character, Double> variableMap = new HashMap<>();
		variableMap.put('x', 14.0);
		variableMap.put('z', 12.0);
		variableMap.put('e', Math.E);
		FunctionParser parser = new FunctionParser("x*(3*x^2-24)*e^x+(z^4/12)");
		System.out.println(parser.evaluate(variableMap));
//		new FunctionParser2("x^(2*x*3*(x+5*x)^2)");
	}
}