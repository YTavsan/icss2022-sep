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
        checkNode(ast.root);
    }

    // Recursively checks the given AST node and its children for semantic errors, managing variable scopes as needed.
    private void checkNode(ASTNode node) {
        if (node instanceof Stylesheet) {
            // global variable scope
            variableTypes.addFirst(new HashMap<>());
            for (ASTNode child : node.getChildren()) {
                checkNode(child);
            }
            // Remove global variable scope
            variableTypes.removeFirst();
        } else if (node instanceof Stylerule) {
            // Add stylerule in scope
            variableTypes.addFirst(new HashMap<>());
            for (ASTNode child : node.getChildren()) {
                checkNode(child);
            }
            // Remove stylerule that is no longer in scope
            variableTypes.removeFirst();
        } else if (node instanceof Declaration) {
            checkDeclaration((Declaration) node);
        } else if (node instanceof VariableAssignment) {
            setScopedVariableAssignment((VariableAssignment) node);
        } else if (node instanceof IfClause) {
            // TODO checkIfClause((IfClause) node);
        } else if (node instanceof ElseClause) {
            // TODO checkElseClause((ElseClause) node);
        } else {
            // For any other type of node, just check its children.
            for (ASTNode child : node.getChildren()) {
                checkNode(child);
            }
        }
    }

    // Function that checks a declaration for semantic errors.
    private void checkDeclaration(Declaration declaration) {
        ExpressionType expressionType = resolveExpressionType(declaration.expression, declaration);
        if (expressionType == ExpressionType.UNDEFINED) {
            return;
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

    private void setScopedVariableAssignment(VariableAssignment variableAssignment) {
        ExpressionType type = resolveExpressionType(variableAssignment.expression, variableAssignment);
        variableTypes.getFirst().put(variableAssignment.name.name, type);
    }

    // Returns the type of the given variable reference.
    private ExpressionType getDefinedVariableReference(VariableReference variableReference) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            if (variableTypes.get(i).containsKey(variableReference.name)) {
                return variableTypes.get(i).get(variableReference.name);
            }
        }
        return ExpressionType.UNDEFINED;
    }

    // Returns the type of the given expression, resolving variables and operations recursively.
    private ExpressionType resolveExpressionType(Expression expression, ASTNode errorNode) {
        ExpressionType expressionType = getSimpleExpressionType(expression);

        if (expressionType != ExpressionType.UNDEFINED) {
            return expressionType;
        } else if (expression instanceof VariableReference) {
            return getDefinedVariableReference((VariableReference) expression);
        } else if (expression instanceof Operation) {
            return getOperationExpressionType((Operation) expression);
        }

        errorNode.setError("Expression '" + expression + "' must be a literal, variable reference, or operation");
        return ExpressionType.UNDEFINED;
    }

    // Gets the type of the given operation by checking the types of its left-hand side and right-hand side expressions and applying the rules for valid operations.
    private ExpressionType getOperationExpressionType(Operation operation) {
        ExpressionType lhsType = resolveExpressionType(operation.lhs, operation);
        if (lhsType == ExpressionType.UNDEFINED) {
            return ExpressionType.UNDEFINED;
        }

        ExpressionType rhsType = resolveExpressionType(operation.rhs, operation);
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
