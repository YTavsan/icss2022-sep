package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.HashMap;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues = new HANLinkedList<>();
        evaluateNode(ast.root);
    }

    // Recursively evaluates the AST, replacing all variable references with literals.
    private void evaluateNode(ASTNode node) {
        if (node instanceof Stylesheet) {
            variableValues.addFirst(new HashMap<>());
            for (ASTNode child : node.getChildren()) {
                evaluateNode(child);
            }
            variableValues.removeFirst();
        } else if (node instanceof Stylerule) {
            variableValues.addFirst(new HashMap<>());
            for (ASTNode child : node.getChildren()) {
                evaluateNode(child);
            }
            variableValues.removeFirst();
        } else if (node instanceof Declaration) {
            Declaration declaration = (Declaration) node;
            declaration.expression = evaluateExpression(declaration.expression);
        } else if (node instanceof VariableAssignment) {
            VariableAssignment variableAssignment = (VariableAssignment) node;
            Literal value = evaluateExpression(variableAssignment.expression);
            variableAssignment.expression = value;
            variableValues.getFirst().put(variableAssignment.name.name, value);
        } else if (node instanceof IfClause) {
            IfClause ifClause = (IfClause) node;
            Literal condition = evaluateExpression(ifClause.conditionalExpression);
            ifClause.conditionalExpression = condition;
            if (condition instanceof BoolLiteral && ((BoolLiteral) condition).value) {
                variableValues.addFirst(new HashMap<>());
                for (ASTNode child : ifClause.body) {
                    evaluateNode(child);
                }
                variableValues.removeFirst();
            } else if (ifClause.elseClause != null) {
                evaluateNode(ifClause.elseClause);
            }
        } else if (node instanceof ElseClause) {
            ElseClause ec = (ElseClause) node;
            variableValues.addFirst(new HashMap<>());
            for (ASTNode child : ec.body) {
                evaluateNode(child);
            }
            variableValues.removeFirst();
        } else {
            // For other nodes keep going through the children.
            for (ASTNode child : node.getChildren()) {
                evaluateNode(child);
            }
        }
    }

    // Evaluates an expression, replacing variable references with literals.
    private Literal evaluateExpression(Expression expression) {
        if (expression instanceof Literal) {
            return (Literal) expression;
        } else if (expression instanceof VariableReference) {
            return getVariableInScope(((VariableReference) expression).name);
        } else if (expression instanceof Operation) {
            return evaluateOperation((Operation) expression);
        }
        return null;
    }

    // Evaluates an operation, replacing variable references with literals.
    private Literal evaluateOperation(Operation operation) {
        Literal lhs = evaluateExpression(operation.lhs);
        Literal rhs = evaluateExpression(operation.rhs);

        if (operation instanceof AddOperation) {
            if (lhs instanceof PixelLiteral && rhs instanceof PixelLiteral) {
                return new PixelLiteral(((PixelLiteral) lhs).value + ((PixelLiteral) rhs).value);
            } else if (lhs instanceof PercentageLiteral && rhs instanceof PercentageLiteral) {
                return new PercentageLiteral(((PercentageLiteral) lhs).value + ((PercentageLiteral) rhs).value);
            } else if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral) {
                return new ScalarLiteral(((ScalarLiteral) lhs).value + ((ScalarLiteral) rhs).value);
            }
        } else if (operation instanceof SubtractOperation) {
            if (lhs instanceof PixelLiteral && rhs instanceof PixelLiteral) {
                return new PixelLiteral(((PixelLiteral) lhs).value - ((PixelLiteral) rhs).value);
            } else if (lhs instanceof PercentageLiteral && rhs instanceof PercentageLiteral) {
                return new PercentageLiteral(((PercentageLiteral) lhs).value - ((PercentageLiteral) rhs).value);
            } else if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral) {
                return new ScalarLiteral(((ScalarLiteral) lhs).value - ((ScalarLiteral) rhs).value);
            }
        } else if (operation instanceof MultiplyOperation) {
            if (lhs instanceof ScalarLiteral && rhs instanceof PixelLiteral) {
                return new PixelLiteral(((ScalarLiteral) lhs).value * ((PixelLiteral) rhs).value);
            } else if (lhs instanceof PixelLiteral && rhs instanceof ScalarLiteral) {
                return new PixelLiteral(((PixelLiteral) lhs).value * ((ScalarLiteral) rhs).value);
            } else if (lhs instanceof ScalarLiteral && rhs instanceof PercentageLiteral) {
                return new PercentageLiteral(((ScalarLiteral) lhs).value * ((PercentageLiteral) rhs).value);
            } else if (lhs instanceof PercentageLiteral && rhs instanceof ScalarLiteral) {
                return new PercentageLiteral(((PercentageLiteral) lhs).value * ((ScalarLiteral) rhs).value);
            } else if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral) {
                return new ScalarLiteral(((ScalarLiteral) lhs).value * ((ScalarLiteral) rhs).value);
            }
        }
        return null;
    }

    // Searches for a variable in the current scope.
    private Literal getVariableInScope(String name) {
        for (int i = 0; i < variableValues.getSize(); i++) {
            if (variableValues.get(i).containsKey(name)) {
                return variableValues.get(i).get(name);
            }
        }
        return null;
    }
}
