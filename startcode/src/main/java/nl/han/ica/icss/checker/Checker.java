package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;


public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    // Checks the given AST for semantic errors. If any are found, they are added to the AST's error list.
    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        checkStyleSheet(ast.root);
    }

    // Checks the given stylesheet for semantic errors. If any are found, they are added to the stylesheet's error list.'
    private void checkStyleSheet(Stylesheet stylesheet) {
        variableTypes.addFirst(new HashMap<>());
        for (var node : stylesheet.getChildren()) {
            if (node instanceof Stylerule) {
                // Create a new scope for the stylerule.
                variableTypes.addFirst(new HashMap<>());
                // Check the stylerule.
                checkStylerule((Stylerule) node);
                // Remove the scope for the stylerule.
                variableTypes.removeFirst();
            } else if (node instanceof VariableAssignment) {
                setScopedVariableAssigment((VariableAssignment) node);
            } else if (node instanceof IfClause) {
                // TODO checkIfClause((IfClause) node);
                System.out.println("IF CLAUSE");
            } else if (node instanceof ElseClause) {
                // TODO checkElseClause((ElseClause) node);
                System.out.println("ELSE CLAUSE");
            }
        }
    }

    // Checks the given stylerule for semantic errors. If any are found, they are added to the stylerule's error list.'
    private void checkStylerule(Stylerule stylerule) {
        for (var node : stylerule.getChildren()) {
            if (node instanceof Declaration) {
                checkDeclaration((Declaration) node);
            } else if (node instanceof VariableAssignment) {
                setScopedVariableAssigment((VariableAssignment) node);
            }
        }
    }

    // Checks the given declaration for semantic errors. If any are found, they are added to the declaration's error list.'
    private void checkDeclaration(Declaration declaration) {
        ExpressionType expressionType;

        // Get the type of the expression.
        expressionType = getSimpleExpressionType(declaration.expression);

        // If the expression type is undefined, it might be a variable reference or an operation.
        if (expressionType == ExpressionType.UNDEFINED) {
            // it might be a variable reference. Try to get the type of the variable reference.
            if (declaration.expression instanceof VariableReference) {
                expressionType = getDefinedVariableReference((VariableReference) declaration.expression);
                if (expressionType == ExpressionType.UNDEFINED) {
                    declaration.setError("Variable reference '" + ((VariableReference) declaration.expression).name + "' is not defined");
                    return;
                }
            }
            // it might be an operation. Try to get the type of the operation.
            else if (declaration.expression instanceof Operation) {
                System.out.println("OPERATION");
                expressionType = getOperationExpressionType((Operation) declaration.expression);
                if (expressionType == ExpressionType.UNDEFINED) {
                    declaration.setError("Operation '" + declaration.property.name + "' is not valid");
                    return;
                }
            } else {
                declaration.setError("Expression '" + declaration.property.name + "'must be a variable reference");
                return;
            }
        }

        // If the property is color or background-color, the expression must be of type color.
        if ((declaration.property.name.equals("color") || declaration.property.name.equals("background-color")) && expressionType != ExpressionType.COLOR) {
            declaration.setError("Color or Background-color property requires a color expression");
            return;
        }

        // If the property is width, the expression must be of type pixel or percentage.
        if (declaration.property.name.equals("width") && expressionType != ExpressionType.PIXEL && expressionType != ExpressionType.PERCENTAGE) {
            declaration.setError("Width property requires a pixel or percentage expression");
        }
    }

    private void setScopedVariableAssigment(VariableAssignment variableAssignment) {
        variableTypes.getFirst().put(variableAssignment.name.name, getSimpleExpressionType(variableAssignment.expression));
    }

    // Returns the type of the given variable reference.
    private ExpressionType getDefinedVariableReference(VariableReference variableReference) {
        for (int i = variableTypes.getSize() - 1; i >= 0; i--) {
            if (variableTypes.get(i).containsKey(variableReference.name)) {
                return variableTypes.get(i).get(variableReference.name);
            }
        }
        return ExpressionType.UNDEFINED;
    }

    // Returns the type of the given operation.
    private ExpressionType resolveExpressionType(Expression expression, Operation currentOperation, boolean isLeftSide) {
        ExpressionType type = getSimpleExpressionType(expression);

        if (type != ExpressionType.UNDEFINED) {
            return type;
        }

        if (expression instanceof VariableReference) {
            type = getDefinedVariableReference((VariableReference) expression);
            if (type == ExpressionType.UNDEFINED) {
                currentOperation.setError("Variable reference '" + ((VariableReference) expression).name + "' is not defined");
            }
            return type;
        }

        if (expression instanceof Operation) {
            type = getOperationExpressionType((Operation) expression);
            if (type == ExpressionType.UNDEFINED) {
                currentOperation.setError("Operation '" + expression + "' is not valid");
            }
            return type;
        }

        currentOperation.setError("Expression '" + expression + "' must be a variable reference or an operation");
        return ExpressionType.UNDEFINED;
    }

    // Gets the type of the given operation by checking the types of its left-hand side and right-hand side expressions and applying the rules for valid operations.
    private ExpressionType getOperationExpressionType(Operation operation) {
        ExpressionType lhsType = resolveExpressionType(operation.lhs, operation, true);
        if (lhsType == ExpressionType.UNDEFINED) {
            return ExpressionType.UNDEFINED;
        }

        ExpressionType rhsType = resolveExpressionType(operation.rhs, operation, false);
        if (rhsType == ExpressionType.UNDEFINED) {
            return ExpressionType.UNDEFINED;
        }

        if (lhsType == ExpressionType.COLOR || rhsType == ExpressionType.COLOR) {
            operation.setError("Operations on colors are not allowed");
            return ExpressionType.UNDEFINED;
        }

        if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            if (lhsType == rhsType && (lhsType == ExpressionType.PIXEL
                    || lhsType == ExpressionType.PERCENTAGE
                    || lhsType == ExpressionType.SCALAR)) {
                return lhsType;
            }
            operation.setError("Addition and subtraction require both operands to be the same type");
            return ExpressionType.UNDEFINED;
        }

        if (operation instanceof MultiplyOperation) {
            if (lhsType == ExpressionType.SCALAR && rhsType == ExpressionType.PIXEL
                    || lhsType == ExpressionType.PIXEL && rhsType == ExpressionType.SCALAR) {
                return ExpressionType.PIXEL;
            }
            if (lhsType == ExpressionType.SCALAR && rhsType == ExpressionType.PERCENTAGE
                    || lhsType == ExpressionType.PERCENTAGE && rhsType == ExpressionType.SCALAR) {
                return ExpressionType.PERCENTAGE;
            }
            if (lhsType == ExpressionType.SCALAR && rhsType == ExpressionType.SCALAR) {
                return ExpressionType.SCALAR;
            }
            operation.setError("Multiplication requires scalar combined with pixel, percentage, or scalar");
            return ExpressionType.UNDEFINED;
        }

        return ExpressionType.UNDEFINED;
    }

    private void checkIfClause(IfClause ifClause) {
        if (ifClause.conditionalExpression instanceof VariableReference) {

        }
        if (ifClause.conditionalExpression instanceof BoolLiteral) {
            if (!((BoolLiteral) ifClause.conditionalExpression).value) {
                ifClause.setError("If clause condition is false, the block will never be executed.");
            }
        } else {
            ifClause.setError("If clause condition must be a boolean literal or variable reference.");
        }
    }

    // Returns the simple type of the given expression.
    private ExpressionType getSimpleExpressionType(Expression expression) {
        if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else {
            return ExpressionType.UNDEFINED;
        }
    }
}
