package de.mrotmann.facharbeitogl.functionparser.seperations.parts;

import de.mrotmann.facharbeitogl.functionparser.seperations.OperatorType;

public class Operator extends AbstractFunctionPart<OperatorType> {
	
	public Operator(OperatorType operator){
		super(operator);
		setName("operator");
	}
	
	public Operator(char operatorChar){
		this(OperatorType.parseChar(operatorChar));
	}
}