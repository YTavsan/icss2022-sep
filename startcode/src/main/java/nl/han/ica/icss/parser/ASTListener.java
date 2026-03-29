package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}

	public AST getAST() {
		return ast;
	}

	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.push(ast.root);
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		// Nothing to do here, the root of the AST is already set in the constructor
	}

	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		Stylerule stylerule = new Stylerule();
		currentContainer.push(stylerule);
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		ASTNode rule = currentContainer.pop();
		currentContainer.peek().addChild(rule);
	}

	@Override
	public void enterClass(ICSSParser.ClassContext ctx) {
		ClassSelector cls = new ClassSelector(ctx.CLASS_IDENT().getText());
		currentContainer.push(cls);
	}

	@Override
	public void exitClass(ICSSParser.ClassContext ctx) {
		ASTNode cls = currentContainer.pop();
		currentContainer.peek().addChild(cls);
	}

	@Override
	public void enterTag(ICSSParser.TagContext ctx) {
		TagSelector tag = new TagSelector(ctx.LOWER_IDENT().getText());
		currentContainer.push(tag);
	}

	@Override
	public void exitTag(ICSSParser.TagContext ctx) {
		ASTNode node = currentContainer.pop();
		currentContainer.peek().addChild(node);
	}

	@Override
	public void enterId(ICSSParser.IdContext ctx) {
		IdSelector id = new IdSelector(ctx.ID_IDENT().getText());
		currentContainer.push(id);
	}

	@Override
	public void exitId(ICSSParser.IdContext ctx) {
		ASTNode id = currentContainer.pop();
		currentContainer.peek().addChild(id);
	}

	@Override
	public void enterAssignment(ICSSParser.AssignmentContext ctx) {
		VariableAssignment assignment = new VariableAssignment();
		currentContainer.push(assignment);
	}

	@Override
	public void exitAssignment(ICSSParser.AssignmentContext ctx) {
		ASTNode assignment = currentContainer.pop();
		currentContainer.peek().addChild(assignment);
	}

	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration declaration = new Declaration();
		currentContainer.push(declaration);
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		ASTNode declaration = currentContainer.pop();
		currentContainer.peek().addChild(declaration);
	}

	@Override
	public void enterProperty(ICSSParser.PropertyContext ctx) {
		PropertyName property = new PropertyName(ctx.LOWER_IDENT().getText());
		currentContainer.push(property);
	}

	@Override
	public void exitProperty(ICSSParser.PropertyContext ctx) {
		ASTNode property = currentContainer.pop();
		currentContainer.peek().addChild(property);
	}

	@Override
	public void enterIf(ICSSParser.IfContext ctx) {
		IfClause ifc = new IfClause();
		currentContainer.push(ifc);
	}

	@Override
	public void exitIf(ICSSParser.IfContext ctx) {
		ASTNode ifc = currentContainer.pop();
		currentContainer.peek().addChild(ifc);
	}

	@Override
	public void enterElse(ICSSParser.ElseContext ctx) {
		ElseClause elsc = new ElseClause();
		currentContainer.push(elsc);
	}

	@Override
	public void exitElse(ICSSParser.ElseContext ctx) {
		ASTNode elsc = currentContainer.pop();
		currentContainer.peek().addChild(elsc);
	}

	@Override
	public void enterVariable(ICSSParser.VariableContext ctx) {
		VariableReference variable = new VariableReference(ctx.CAPITAL_IDENT().getText());
		currentContainer.push(variable);
	}

	@Override
	public void exitVariable(ICSSParser.VariableContext ctx) {
		ASTNode variable = currentContainer.pop();
		currentContainer.peek().addChild(variable);
	}

	@Override
	public void enterScalar(ICSSParser.ScalarContext ctx) {
		ScalarLiteral sl = new ScalarLiteral(Integer.parseInt(ctx.SCALAR().getText()));
		currentContainer.push(sl);
	}

	@Override
	public void exitScalar(ICSSParser.ScalarContext ctx) {
		ASTNode node = currentContainer.pop();
		currentContainer.peek().addChild(node);
	}

	@Override
	public void enterPixel(ICSSParser.PixelContext ctx) {
		PixelLiteral pixel = new PixelLiteral(ctx.PIXELSIZE().getText());
		currentContainer.push(pixel);
	}

	@Override
	public void exitPixel(ICSSParser.PixelContext ctx) {
		ASTNode pixel = currentContainer.pop();
		currentContainer.peek().addChild(pixel);
	}

	@Override
	public void enterPercentage(ICSSParser.PercentageContext ctx) {
		PercentageLiteral percentage = new PercentageLiteral(ctx.PERCENTAGE().getText());
		currentContainer.push(percentage);
	}

	@Override
	public void exitPercentage(ICSSParser.PercentageContext ctx) {
		ASTNode percentage = currentContainer.pop();
		currentContainer.peek().addChild(percentage);
	}

	@Override
	public void enterColor(ICSSParser.ColorContext ctx) {
		ColorLiteral color = new ColorLiteral(ctx.COLOR().getText());
		currentContainer.push(color);
	}

	@Override
	public void exitColor(ICSSParser.ColorContext ctx) {
		ASTNode color = currentContainer.pop();
		currentContainer.peek().addChild(color);
	}

	@Override
	public void enterBoolean(ICSSParser.BooleanContext ctx) {
		BoolLiteral bool = new BoolLiteral(ctx.TRUE() != null);
		currentContainer.push(bool);
	}

	@Override
	public void exitBoolean(ICSSParser.BooleanContext ctx) {
		ASTNode bool = currentContainer.pop();
		currentContainer.peek().addChild(bool);
	}

	@Override
	public void enterMultiply(ICSSParser.MultiplyContext ctx) {
		MultiplyOperation mult = new MultiplyOperation();
		currentContainer.push(mult);
	}

	@Override
	public void exitMultiply(ICSSParser.MultiplyContext ctx) {
		ASTNode mult = currentContainer.pop();
		currentContainer.peek().addChild(mult);
	}

	@Override
	public void enterSum(ICSSParser.SumContext ctx) {
		AddOperation sum = new AddOperation();
		currentContainer.push(sum);
	}

	@Override
	public void exitSum(ICSSParser.SumContext ctx) {
		ASTNode sum = currentContainer.pop();
		currentContainer.peek().addChild(sum);
	}

	@Override
	public void enterSub(ICSSParser.SubContext ctx) {
		SubtractOperation sub = new SubtractOperation();
		currentContainer.push(sub);
	}

	@Override
	public void exitSub(ICSSParser.SubContext ctx) {
		ASTNode sub = currentContainer.pop();
		currentContainer.peek().addChild(sub);
	}
}
