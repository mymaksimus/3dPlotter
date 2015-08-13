package de.mrotmann.facharbeitogl.functionparser.seperations;

public enum OperatorType {
	
	ADD(0, '+', new OperatorEvaluation(){
		public Double evaluate(Double d1, Double d2){
			return d1 + d2;
		}
	}),
	SUBTRACT(0, '-', new OperatorEvaluation(){
		public Double evaluate(Double d1, Double d2){
			return d1 - d2;
		}
	}),
	MODULO(1, '%', new OperatorEvaluation(){
		public Double evaluate(Double d1, Double d2){
			return d1 % d2;
		}
	}),
	MULTIPLY(1, '*', new OperatorEvaluation(){
		public Double evaluate(Double d1, Double d2){
			return d1 * d2;
		}
	}), 
	DIVIDE(1, '/', new OperatorEvaluation(){
		public Double evaluate(Double d1, Double d2){
			return d1 / d2;
		}
	}),
	EXPONENT(2, '^', new OperatorEvaluation(){
		public Double evaluate(Double d1, Double d2){
			return Math.pow(d1, d2);
		};
	});
	
	private char operatorChar;
	private OperatorEvaluation evaluation;
	private int priority;
	public static final int MAX_PRIORITY = 2;
	
	OperatorType(int priority, char operatorString, OperatorEvaluation evaluation){
		this.operatorChar = operatorString;
		this.evaluation = evaluation;
		this.priority = priority;
	}
	
	public static OperatorType parseChar(char operatorString){
		OperatorType types[] = OperatorType.values();
		for(OperatorType type : types){
			if(type.getOperatorChar() == operatorString) return type;
		}
		return null;
	}
	
	public Double evaluate(Double d1, Double d2){
		return evaluation.evaluate(d1, d2);
	}
	
	public int getPriority(){
		return priority;
	}
	
	public char getOperatorChar(){
		return operatorChar;
	}
	
	static interface OperatorEvaluation {
		Double evaluate(Double d1, Double d2);
	}
}