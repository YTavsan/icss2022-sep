package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.selectors.TagSelector;
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
}
