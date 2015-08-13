package de.mrotmann.facharbeitogl.functionparser.seperations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.Predicate;

import de.mrotmann.facharbeitogl.functionparser.seperations.parts.AbstractFunctionPart;
import de.mrotmann.facharbeitogl.functionparser.seperations.parts.FunctionPart;
import de.mrotmann.facharbeitogl.functionparser.seperations.parts.MathematicFunction;
import de.mrotmann.facharbeitogl.functionparser.seperations.parts.Operator;
import de.mrotmann.facharbeitogl.functionparser.seperations.parts.SingleNumber;
import de.mrotmann.facharbeitogl.functionparser.seperations.parts.Variable;

public class BraceElementSeperation extends AbstractFunctionPart<ArrayList<FunctionPart<?>>> {
	
	private BraceElementSeperation parent;
	private ArrayList<FunctionPart<?>> functionParts;
	private StringBuilder singleNumberBuffer, variableCharacterBuilder;
	
	public BraceElementSeperation(){
		super(null);
		functionParts = new ArrayList<>();
		setName("BSE");
		setValue(functionParts);
		singleNumberBuffer = new StringBuilder();
		variableCharacterBuilder = new StringBuilder();
	}
	
	public void setParent(BraceElementSeperation parent){
		this.parent = parent;
	}
	
	public BraceElementSeperation getParent(){
		return parent;
	}
	
	public void addSubBraceSeperation(BraceElementSeperation seperation){
		addFunctionPart(seperation);
	}
	
	private void addFunctionPart(FunctionPart<?> part){
		functionParts.add(part);
	}
	
	private void subSeperationIf(Predicate<FunctionPart<?>> predicate, int elementsBefore, int elementsAfter){
		ListIterator<FunctionPart<?>> iterator = functionParts.listIterator();
		while(iterator.hasNext()){
			FunctionPart<?> part = iterator.next();
			if(predicate.test(part)){
				BraceElementSeperation subSeperation = new BraceElementSeperation();
				for(int i = 0; i < elementsBefore + 1; i++){
					iterator.previous();
				}
				for(int i = 0; i < elementsBefore + elementsAfter + 1; i++){
					subSeperation.addFunctionPart(iterator.next());
					iterator.remove();
				}
				iterator.add(subSeperation);
			}
		}
	}
	
	public void endBuild(){
		finishNumberIfExisting();
		finishCharsIfExisting();
		ListIterator<FunctionPart<?>> iterator = functionParts.listIterator();
		while(iterator.hasNext()){
			FunctionPart<?> current = iterator.next();
			FunctionPart<?> next = null;
			if(iterator.hasNext()){
				next = iterator.next();
			}
			if(next != null){
				boolean operatorLessParts = !(current instanceof Operator || next instanceof Operator) && !(current instanceof MathematicFunction);
				if(operatorLessParts){
					iterator.previous();
					iterator.add(new Operator(OperatorType.MULTIPLY));
				}
			}
		}
		subSeperationIf(new Predicate<FunctionPart<?>>(){
			public boolean test(FunctionPart<?> t){
				return t instanceof MathematicFunction;
			}
		}, 0, 1);
		for(int i = OperatorType.MAX_PRIORITY; i > 0; i--){
			final int iCopy = i;
			subSeperationIf(new Predicate<FunctionPart<?>>(){
				public boolean test(FunctionPart<?> t){
					return t instanceof Operator && ((Operator) t).getValue().getPriority() == iCopy;
				}
			}, 1, 1);
		}
	}
	
	public double evaluate(HashMap<Character, Double> variableMap){
		OperatorType currentOperatorType = OperatorType.ADD;
		double result = 0;
		Iterator<FunctionPart<?>> iterator = functionParts.iterator();
		while(iterator.hasNext()){
			FunctionPart<?> part = iterator.next();
			if(part instanceof Operator){
				currentOperatorType = ((Operator) part).getValue();
			}
			else {
				double nextValue = 0;
				if(part instanceof BraceElementSeperation){
					nextValue = ((BraceElementSeperation) part).evaluate(variableMap);
				}
				if(part instanceof Variable){
					nextValue = variableMap.get(((Variable) part).getValue()).doubleValue();
				}
				else if(part instanceof SingleNumber){
					nextValue = ((SingleNumber) part).getValue();
				}
				else if(part instanceof MathematicFunction){
					nextValue = ((MathematicFunction) part).getValue().evaluate(((BraceElementSeperation) iterator.next()).evaluate(variableMap));
				}
				result = currentOperatorType.evaluate(result, nextValue);
			}
		}
		return result;
	}
	
	public void newBraceElementRequested(){
		finishCharsIfExisting();
	}
	
	public void finishCharsIfExisting(){
		if(variableCharacterBuilder.length() > 0){
			finishChars();
		}
	}
	
	public void finishNumberIfExisting(){
		if(singleNumberBuffer.length() > 0){
			finishSingleNumber();
		}
	}
	
	private void finishChars(){
		String variableString = variableCharacterBuilder.toString();
		MathematicFunction function = new MathematicFunction(variableString);
		if(function.getValue() != null){
			functionParts.add(function);
		}
		else {
			char chars[] = variableString.toCharArray();
			for(char c : chars){
				functionParts.add(new Variable(c));
			}
		}
		variableCharacterBuilder = new StringBuilder();
	}
	
	private void finishSingleNumber(){
		Double currentNumber = Double.parseDouble(singleNumberBuffer.toString());
		SingleNumber number = new SingleNumber(currentNumber);
		functionParts.add(number);
		singleNumberBuffer = new StringBuilder();
	}
	
	public void parseChar(char c){
		Class<? extends FunctionPart<?>> functionPartType = FunctionPartParser.parseFunctionChar(c);
		if(functionPartType == SingleNumber.class){
			singleNumberBuffer.append(c);
		}
		else {
			finishNumberIfExisting();
		}
		if(functionPartType == Variable.class){
			variableCharacterBuilder.append(c);
		}
		else {
			finishCharsIfExisting();
		}
		if(functionPartType == Operator.class){
			functionParts.add(new Operator(c));
		}
	}
	
	public ArrayList<FunctionPart<?>> getValue(){
		return functionParts;
	}
}