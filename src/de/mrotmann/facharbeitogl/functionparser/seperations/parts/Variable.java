package de.mrotmann.facharbeitogl.functionparser.seperations.parts;

public class Variable extends AbstractFunctionPart<Character> {
	
	public Variable(Character variableChar){
		super(variableChar);
		setName("FunctionVariable");
	}
}