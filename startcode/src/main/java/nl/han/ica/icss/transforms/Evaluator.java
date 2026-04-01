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
            evaluateStyleruleBody((Stylerule) node);
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
            evaluateIfClause((IfClause) node);
        } else if (node instanceof ElseClause) {
            ElseClause elseClause = (ElseClause) node;
            for (ASTNode child : elseClause.body) {
                evaluateNode(child);
            }
        } else {
            // For other nodes keep going through the children.
            for (ASTNode child : node.getChildren()) {
                evaluateNode(child);
            }
        }
    }

    // Function that evaluates the body of a stylerule, replacing variable references with literals.
    private void evaluateStyleruleBody(Stylerule stylerule) {
        for (int i = 0; i < stylerule.body.size(); i++) {
            ASTNode child = stylerule.body.get(i);
            if (child instanceof IfClause) {
                IfClause ifClause = (IfClause) child;
                Literal condition = evaluateExpression(ifClause.conditionalExpression);
                ifClause.conditionalExpression = condition;

                if (condition instanceof BoolLiteral && ((BoolLiteral) condition).value) {
                    // Flatten the true branch into the parent body
                    stylerule.body.remove(i);
                    for (int j = 0; j < ifClause.body.size(); j++) {
                        ASTNode bodyNode = ifClause.body.get(j);
                        evaluateNode(bodyNode);
                        stylerule.body.add(i + j, bodyNode);
                    }
                    i += ifClause.body.size() - 1;
                } else if (ifClause.elseClause != null) {
                    // Flatten the else branch into the parent body
                    stylerule.body.remove(i);
                    for (int j = 0; j < ifClause.elseClause.body.size(); j++) {
                        ASTNode bodyNode = ifClause.elseClause.body.get(j);
                        evaluateNode(bodyNode);
                        stylerule.body.add(i + j, bodyNode);
                    }
                    i += ifClause.elseClause.body.size() - 1;
                } else {
                    // No condition matched and no else clause, remove the IfClause
                    stylerule.body.remove(i);
                    i--;
                }
            } else {
                evaluateNode(child);
            }
        }
    }

    // Function that evaluates an IfClause, replacing variable references with literals.
    private void evaluateIfClause(IfClause ifClause) {
        Literal condition = evaluateExpression(ifClause.conditionalExpression);
        ifClause.conditionalExpression = condition;
        if (condition instanceof BoolLiteral && ((BoolLiteral) condition).value) {
            for (ASTNode child : ifClause.body) {
                evaluateNode(child);
            }
        } else if (ifClause.elseClause != null) {
            evaluateNode(ifClause.elseClause);
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
