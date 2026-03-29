package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
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
                variableTypes.addFirst(new HashMap<>());
                checkStylerule((Stylerule) node);
                variableTypes.removeFirst();
            } else if (node instanceof VariableAssignment) {
                // TODO checkVariableAssignment((VariableAssignment) node);
            } else if (node instanceof IfClause) {
                // TODO checkIfClause((IfClause) node);
            } else if (node instanceof ElseClause) {
                // TODO checkElseClause((ElseClause) node);
            }
        }
    }

    // Checks the given stylerule for semantic errors. If any are found, they are added to the stylerule's error list.'
    private void checkStylerule(Stylerule stylerule) {
        for (var node : stylerule.getChildren()) {
            if (node instanceof Declaration) {
                checkDeclaration((Declaration) node);
            }
        }
    }

    // Checks the given declaration for semantic errors. If any are found, they are added to the declaration's error list.'
    private void checkDeclaration(Declaration declaration) {
        ExpressionType expressionType;

        expressionType = expressionType(declaration.expression);

        // If the expression type is undefined, the declaration is invalid.
        if (expressionType == ExpressionType.UNDEFINED) {
            declaration.setError("Undefined expression type");
        }

        // If the property is color or background-color, the expression must be of type color.
        if ((declaration.property.name.equals("color") || declaration.property.name.equals("background-color")) && expressionType != ExpressionType.COLOR) {
            declaration.setError("Color or Background-color property requires a color expression");
        }

        // If the property is width, the expression must be of type pixel or percentage.
        if (declaration.property.name.equals("width") && expressionType != ExpressionType.PIXEL && expressionType != ExpressionType.PERCENTAGE) {
            declaration.setError("Width property requires a pixel or percentage expression");
        }
    }

    private ExpressionType expressionType(Expression expression) {
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
