package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.selectors.TagSelector;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

// Testcases for the Checker class, which performs semantic analysis on the AST of an ICSS stylesheet.
public class CheckerTest {

    private Checker checker;
    private AST ast;

    @BeforeEach
    public void setUp() {
        checker = new Checker();
        ast = new AST();
    }

    @Test
    public void testCH01UndeclaredVariableDeclaration() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("p"));

        Declaration declaration = new Declaration("color");
        declaration.expression = new VariableReference("UndeclaredVar");
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.toString().contains("UndeclaredVar") && e.toString().contains("not defined")),
                "Should have error for undeclared variable reference");
    }

    @Test
    public void testCH01DeclaredVariableDeclaration() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();

        VariableAssignment varAssignment = new VariableAssignment();
        varAssignment.name = new VariableReference("MyColor");
        varAssignment.expression = new ColorLiteral("#ff0000");
        stylesheet.body.add(varAssignment);

        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("p"));

        Declaration declaration = new Declaration("color");
        declaration.expression = new VariableReference("MyColor");
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Should not have error for variable declared at stylesheet level");
    }

    @Test
    public void testCH06DeclaredVariableWithinSameScope() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("p"));

        VariableAssignment varAssignment = new VariableAssignment();
        varAssignment.name = new VariableReference("LocalVar");
        varAssignment.expression = new ColorLiteral("#0000ff");
        stylerule.body.add(varAssignment);

        Declaration declaration = new Declaration("color");
        declaration.expression = new VariableReference("LocalVar");
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Should not have error for variable declared within same stylerule");
    }

    @Test
    public void testCH06DeclaredVariableInOtherScope() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();

        Stylerule stylerule1 = new Stylerule();
        stylerule1.selectors.add(new TagSelector("p"));

        VariableAssignment varAssignment = new VariableAssignment();
        varAssignment.name = new VariableReference("ScopedVar");
        varAssignment.expression = new PixelLiteral(10);
        stylerule1.body.add(varAssignment);

        stylesheet.body.add(stylerule1);

        Stylerule stylerule2 = new Stylerule();
        stylerule2.selectors.add(new TagSelector("a"));

        Declaration declaration = new Declaration("width");
        declaration.expression = new VariableReference("ScopedVar");
        stylerule2.body.add(declaration);

        stylesheet.body.add(stylerule2);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.toString().contains("ScopedVar") && e.toString().contains("not defined")),
                "Variable declared in one stylerule should not be accessible in another");
    }

    @Test
    public void testCH04ColorPropertyPixelTypeShouldNotMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("p"));

        Declaration declaration = new Declaration("color");
        declaration.expression = new PixelLiteral(100);
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.toString().contains("Color") && e.toString().contains("color expression")),
                "Color property must have color expression");
    }

    @Test
    public void testCH04ColorPropertyPercentageTypeShouldNotMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("a"));

        Declaration declaration = new Declaration("color");
        declaration.expression = new PercentageLiteral(50);
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.toString().contains("Color") && e.toString().contains("color expression")),
                "Color property must have color expression, not percentage");
    }

    @Test
    public void testCH04ColorPropertyColorTypeShouldMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("p"));

        Declaration declaration = new Declaration("color");
        declaration.expression = new ColorLiteral("#ff0000");
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Color property with color value should not have error");
    }

    @Test
    public void testCH04BackgroundColorPropertyColorTypeShouldMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("a"));

        Declaration declaration = new Declaration("background-color");
        declaration.expression = new ColorLiteral("#ffffff");
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Background-color property with color value should not have error");
    }

    @Test
    public void testCH04BackgroundColorPropertyPixelTypeShouldNotMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("a"));

        Declaration declaration = new Declaration("background-color");
        declaration.expression = new PixelLiteral(50);
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert: Should have error
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.toString().contains("Background-color") && e.toString().contains("color expression")),
                "Background-color property must have color expression");
    }

    @Test
    public void testCH04WidthPropertyPixelTypeShouldMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("p"));

        Declaration declaration = new Declaration("width");
        declaration.expression = new PixelLiteral(500);
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Width property with pixel value should not have error");
    }

    @Test
    public void testCH04WidthPropertyPercentageTypeShouldMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("a"));

        Declaration declaration = new Declaration("width");
        declaration.expression = new PercentageLiteral(75);
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Width property with percentage value should not have error");
    }

    @Test
    public void testCH04WidthPropertyColorTypeShouldNotMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("a"));

        Declaration declaration = new Declaration("width");
        declaration.expression = new ColorLiteral("#ff0000");
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.toString().contains("Width") && e.toString().contains("pixel or percentage expression")),
                "Width property must have pixel or percentage expression");
    }

    @Test
    public void testCH04WidthPropertyColorVariableTypeShouldNotMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();

        VariableAssignment varAssignment = new VariableAssignment();
        varAssignment.name = new VariableReference("MyColor");
        varAssignment.expression = new ColorLiteral("#ff0000");
        stylesheet.body.add(varAssignment);

        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("p"));

        Declaration declaration = new Declaration("width");
        declaration.expression = new VariableReference("MyColor");
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.toString().contains("Width") && e.toString().contains("pixel or percentage expression")),
                "Width property cannot use color variable");
    }

    @Test
    public void testCH04MultipleDeclarationsTypeShouldNotMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();

        VariableAssignment colorVar = new VariableAssignment();
        colorVar.name = new VariableReference("LinkColor");
        colorVar.expression = new ColorLiteral("#ff0000");
        stylesheet.body.add(colorVar);

        VariableAssignment widthVar = new VariableAssignment();
        widthVar.name = new VariableReference("PageWidth");
        widthVar.expression = new PixelLiteral(1200);
        stylesheet.body.add(widthVar);

        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("p"));

        Declaration colorDecl = new Declaration("color");
        colorDecl.expression = new VariableReference("LinkColor");
        stylerule.body.add(colorDecl);

        Declaration widthDecl = new Declaration("a");
        widthDecl.expression = new VariableReference("PageWidth");
        stylerule.body.add(widthDecl);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Multiple declarations with correct types should not have errors");
    }

    @Test
    public void testCHnEmptyStylesheetShouldNotHaveErrors() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Empty stylesheet should not have errors");
    }

    @Test
    public void testCH07ColorTypeInAddOperationShouldNotBeAllowed() {
        // Setup: Addition operation with color operand
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("p"));

        Declaration declaration = new Declaration("width");
        AddOperation addOp = new AddOperation();
        addOp.lhs = new ColorLiteral("#ff0000");
        addOp.rhs = new PixelLiteral(10);
        declaration.expression = addOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.toString().toLowerCase().contains("color") && e.toString().toLowerCase().contains("operation")),
                "Color types should not be allowed in operations");
    }

    @Test
    public void testCH03ColorTypeInSubtractOperationShouldNotBeAllowed() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("div"));

        Declaration declaration = new Declaration("width");
        SubtractOperation subOp = new SubtractOperation();
        subOp.lhs = new PixelLiteral(50);
        subOp.rhs = new ColorLiteral("#000000");
        declaration.expression = subOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.toString().toLowerCase().contains("color") && e.toString().toLowerCase().contains("operation")),
                "Color types should not be allowed in operations");
    }

    @Test
    public void testCH03ColorTypeInMultiplyOperationShouldNotBeAllowed() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("a"));

        Declaration declaration = new Declaration("width");
        MultiplyOperation mulOp = new MultiplyOperation();
        mulOp.lhs = new ColorLiteral("#ff0000");
        mulOp.rhs = new ScalarLiteral(3);
        declaration.expression = mulOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.toString().toLowerCase().contains("color") && e.toString().toLowerCase().contains("operation")),
                "Color types should not be allowed in operations");
    }

    @Test
    public void testCH02AdditionPixelPlusPixelShouldMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("p"));

        Declaration declaration = new Declaration("width");
        AddOperation addOp = new AddOperation();
        addOp.lhs = new PixelLiteral(100);
        addOp.rhs = new PixelLiteral(50);
        declaration.expression = addOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Addition of same types (pixel + pixel) should be allowed");
    }

    @Test
    public void testCH02AdditionPercentagePlusPercentageShouldMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("div"));

        Declaration declaration = new Declaration("width");
        AddOperation addOp = new AddOperation();
        addOp.lhs = new PercentageLiteral(50);
        addOp.rhs = new PercentageLiteral(25);
        declaration.expression = addOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Addition of same types (percentage + percentage) should be allowed");
    }

    @Test
    public void testCH02AdditionPixelPlusPercentageShouldNotMatch() {
        // Setup: Addition of different types (pixel + percentage)
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("span"));

        Declaration declaration = new Declaration("width");
        AddOperation addOp = new AddOperation();
        addOp.lhs = new PixelLiteral(100);
        addOp.rhs = new PercentageLiteral(50);
        declaration.expression = addOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.toString().toLowerCase().contains("require") && e.toString().toLowerCase().contains("same type")),
                "Addition requires operands of same type");
    }

    @Test
    public void testCH02SubtractionPixelMinusPixelShouldMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("p"));

        Declaration declaration = new Declaration("width");
        SubtractOperation subOp = new SubtractOperation();
        subOp.lhs = new PixelLiteral(100);
        subOp.rhs = new PixelLiteral(50);
        declaration.expression = subOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Subtraction of same types (pixel - pixel) should be allowed");
    }

    @Test
    public void testCH02SubtractionPercentageMinusPercentageShouldMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("a"));

        Declaration declaration = new Declaration("width");
        SubtractOperation subOp = new SubtractOperation();
        subOp.lhs = new PercentageLiteral(75);
        subOp.rhs = new PercentageLiteral(25);
        declaration.expression = subOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Subtraction of same types (percentage - percentage) should be allowed");
    }

    @Test
    public void testCH02SubtractionPixelMinusPercentageShouldNotMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("section"));

        Declaration declaration = new Declaration("width");
        SubtractOperation subOp = new SubtractOperation();
        subOp.lhs = new PixelLiteral(100);
        subOp.rhs = new PercentageLiteral(50);
        declaration.expression = subOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.toString().toLowerCase().contains("require") && e.toString().toLowerCase().contains("same type")),
                "Subtraction requires operands of same type");
    }

    @Test
    public void testCH02MultiplicationPercentageTimesScalarShouldMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("p"));

        Declaration declaration = new Declaration("width");
        MultiplyOperation mulOp = new MultiplyOperation();
        mulOp.lhs = new PercentageLiteral(20);
        mulOp.rhs = new ScalarLiteral(3);
        declaration.expression = mulOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Multiplication with one scalar operand should be allowed (20% * 3)");
    }

    @Test
    public void testCH02MultiplicationScalarTimesPixelShouldMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("div"));

        Declaration declaration = new Declaration("width");
        MultiplyOperation mulOp = new MultiplyOperation();
        mulOp.lhs = new ScalarLiteral(5);
        mulOp.rhs = new PixelLiteral(10);
        declaration.expression = mulOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Multiplication with one scalar operand should be allowed (5 * 10px)");
    }

    @Test
    public void testCH02MultiplicationScalarTimesScalarShouldMatch() {
        // Setup: Multiplication with both scalars (scalar * scalar)

        // While scalar * scalar is a valid operation it's not for a width proeperty
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("a"));

        Declaration declaration = new Declaration("width");
        MultiplyOperation mulOp = new MultiplyOperation();
        mulOp.lhs = new ScalarLiteral(4);
        mulOp.rhs = new ScalarLiteral(2);
        declaration.expression = mulOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        // The scalar * scalar operation itself is valid, but using SCALAR result for width property should fail
        assertTrue(errors.stream().anyMatch(e -> e.toString().toLowerCase().contains("width") && e.toString().toLowerCase().contains("pixel or percentage")),
                "Width property cannot use scalar result (scalar * scalar = scalar)");
    }

    @Test
    public void testCH02MultiplicationPixelTimesPixelShouldNotMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("p"));

        Declaration declaration = new Declaration("width");
        MultiplyOperation mulOp = new MultiplyOperation();
        mulOp.lhs = new PixelLiteral(10);
        mulOp.rhs = new PixelLiteral(5);
        declaration.expression = mulOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.toString().contains("scalar") || e.toString().contains("one scalar")),
                "Multiplication without scalar operand should not be allowed (2px * 2px not allowed)");
    }

    @Test
    public void testCH02MultiplicationPercentageTimesPercentageShouldNotMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();
        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("div"));

        Declaration declaration = new Declaration("width");
        MultiplyOperation mulOp = new MultiplyOperation();
        mulOp.lhs = new PercentageLiteral(50);
        mulOp.rhs = new PercentageLiteral(75);
        declaration.expression = mulOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.toString().contains("scalar") || e.toString().contains("one scalar")),
                "Multiplication without scalar operand should not be allowed");
    }

    @Test
    public void testCH02AdditionWithVariablesOfSameTypeShouldMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();

        VariableAssignment varAssignment1 = new VariableAssignment();
        varAssignment1.name = new VariableReference("Width1");
        varAssignment1.expression = new PixelLiteral(100);
        stylesheet.body.add(varAssignment1);

        VariableAssignment varAssignment2 = new VariableAssignment();
        varAssignment2.name = new VariableReference("Width2");
        varAssignment2.expression = new PixelLiteral(50);
        stylesheet.body.add(varAssignment2);

        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("p"));

        Declaration declaration = new Declaration("width");
        AddOperation addOp = new AddOperation();
        addOp.lhs = new VariableReference("Width1");
        addOp.rhs = new VariableReference("Width2");
        declaration.expression = addOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Addition of variables with same type should be allowed");
    }

    @Test
    public void testCH02MultiplicationWithVariableScalarShouldMatch() {
        // Setup
        Stylesheet stylesheet = new Stylesheet();

        VariableAssignment varAssignment = new VariableAssignment();
        varAssignment.name = new VariableReference("Multiplier");
        varAssignment.expression = new ScalarLiteral(3);
        stylesheet.body.add(varAssignment);

        Stylerule stylerule = new Stylerule();
        stylerule.selectors.add(new TagSelector("div"));

        Declaration declaration = new Declaration("width");
        MultiplyOperation mulOp = new MultiplyOperation();
        mulOp.lhs = new VariableReference("Multiplier");
        mulOp.rhs = new PixelLiteral(20);
        declaration.expression = mulOp;
        stylerule.body.add(declaration);

        stylesheet.body.add(stylerule);
        ast.setRoot(stylesheet);

        // Execute
        checker.check(ast);

        // Assert
        ArrayList<SemanticError> errors = ast.getErrors();
        assertTrue(errors.isEmpty(),
                "Multiplication with variable scalar should be allowed");
    }
}
