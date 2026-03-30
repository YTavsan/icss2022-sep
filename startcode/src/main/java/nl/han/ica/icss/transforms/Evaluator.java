package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.HashMap;
import java.util.LinkedList;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues = new HANLinkedList<>();
        evaluate(ast.root);

    }

    private void evaluate(Stylesheet stylesheet) {
        variableValues.addFirst(new HashMap<>());
        for (ASTNode childNode : stylesheet.getChildren()) {
            if (childNode instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) childNode);
            } else if (childNode instanceof Stylerule) {
                variableValues.addFirst(new HashMap<>());
                evaluateStylerule((Stylerule) childNode);
                variableValues.removeFirst();
            }
        }
    }

    private void evaluateStylerule(Stylerule stylerule) {
        for (ASTNode childNode : stylerule.getChildren()) {
            if (childNode instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) childNode);
            } else if (childNode instanceof Declaration) {
                evaluateDeclaration((Declaration) childNode);
            } else if (childNode instanceof IfClause) {
                evaluateIfClause((IfClause) childNode);
            } else if (childNode instanceof ElseClause) {
                evaluateElseClause((ElseClause) childNode);
            }
        }
    }

    private void evaluateVariableAssignment(VariableAssignment variableAssignment) {
        if (variableAssignment.expression instanceof Literal) {
            variableValues.getFirst().put(variableAssignment.name.name, (Literal) variableAssignment.expression);
        } else if (variableAssignment.expression instanceof VariableReference) {
            variableAssignment.expression = getVariableInScope(((VariableReference) variableAssignment.expression).name);
            variableValues.getFirst().put(variableAssignment.name.name, (Literal) variableAssignment.expression);
        } else if (variableAssignment.expression instanceof Operation) {
            variableAssignment.expression = getOperationResult((Operation) variableAssignment.expression);
            variableValues.getFirst().put(variableAssignment.name.name, (Literal) variableAssignment.expression);
        }
    }


}
