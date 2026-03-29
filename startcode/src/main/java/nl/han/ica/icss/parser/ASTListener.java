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
		// Return the AST
		return ast;
	}

	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		//
		currentContainer.push(ast.root);
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		// Nothing to do here, the root of the AST is already set in the constructor
	}

	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		// Create a new Stylerule and push it to the stack, so that the following nodes will be added as children to it until we leave the stylerule
		Stylerule stylerule = new Stylerule();
		currentContainer.push(stylerule);
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		// When we leave the stylerule, we pop it from the stack and add it as a child to the current top of the stack (which is the root of the AST or another stylerule)
		ASTNode rule = currentContainer.pop();
		currentContainer.peek().addChild(rule);
	}

	@Override
	public void enterClass(ICSSParser.ClassContext ctx) {
		// Create a new ClassSelector and push it to the stack, so that the following nodes will be added as children to it until we leave the class
		ClassSelector cls = new ClassSelector(ctx.CLASS_IDENT().getText());
		currentContainer.push(cls);
	}

	@Override
	public void exitClass(ICSSParser.ClassContext ctx) {
		// When we leave the class, we pop it from the stack and add it as a child to the current top of the stack (which is the root of the AST or another stylerule)
		ASTNode cls = currentContainer.pop();
		currentContainer.peek().addChild(cls);
	}

	@Override
	public void enterTag(ICSSParser.TagContext ctx) {
		// idem
		TagSelector tag = new TagSelector(ctx.LOWER_IDENT().getText());
		currentContainer.push(tag);
	}

	@Override
	public void exitTag(ICSSParser.TagContext ctx) {
		// idem
		ASTNode node = currentContainer.pop();
		currentContainer.peek().addChild(node);
	}

	@Override
	public void enterId(ICSSParser.IdContext ctx) {
		// idem
		IdSelector id = new IdSelector(ctx.ID_IDENT().getText());
		currentContainer.push(id);
	}

	@Override
	public void exitId(ICSSParser.IdContext ctx) {
		// idem
		ASTNode id = currentContainer.pop();
		currentContainer.peek().addChild(id);
	}

	@Override
	public void enterAssignment(ICSSParser.AssignmentContext ctx) {
		// idem
		VariableAssignment assignment = new VariableAssignment();
		currentContainer.push(assignment);
	}

	@Override
	public void exitAssignment(ICSSParser.AssignmentContext ctx) {
		// idem
		ASTNode assignment = currentContainer.pop();
		currentContainer.peek().addChild(assignment);
	}

	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		// idem
		Declaration declaration = new Declaration();
		currentContainer.push(declaration);
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		// idem
		ASTNode declaration = currentContainer.pop();
		currentContainer.peek().addChild(declaration);
	}

	@Override
	public void enterProperty(ICSSParser.PropertyContext ctx) {
		// idem
		PropertyName property = new PropertyName(ctx.LOWER_IDENT().getText());
		currentContainer.push(property);
	}

	@Override
	public void exitProperty(ICSSParser.PropertyContext ctx) {
		// idem
		ASTNode property = currentContainer.pop();
		currentContainer.peek().addChild(property);
	}

	@Override
	public void enterIf(ICSSParser.IfContext ctx) {
		// idem
		IfClause ifc = new IfClause();
		currentContainer.push(ifc);
	}

	@Override
	public void exitIf(ICSSParser.IfContext ctx) {
		// idem
		ASTNode ifc = currentContainer.pop();
		currentContainer.peek().addChild(ifc);
	}

	@Override
	public void enterElse(ICSSParser.ElseContext ctx) {
		// idem
		ElseClause elsc = new ElseClause();
		currentContainer.push(elsc);
	}

	@Override
	public void exitElse(ICSSParser.ElseContext ctx) {
		// idem
		ASTNode elsc = currentContainer.pop();
		currentContainer.peek().addChild(elsc);
	}

	@Override
	public void enterVariable(ICSSParser.VariableContext ctx) {
		// idem
		VariableReference variable = new VariableReference(ctx.CAPITAL_IDENT().getText());
		currentContainer.push(variable);
	}

	@Override
	public void exitVariable(ICSSParser.VariableContext ctx) {
		// idem
		ASTNode variable = currentContainer.pop();
		currentContainer.peek().addChild(variable);
	}

	@Override
	public void enterScalar(ICSSParser.ScalarContext ctx) {
		// idem
		ScalarLiteral sl = new ScalarLiteral(Integer.parseInt(ctx.SCALAR().getText()));
		currentContainer.push(sl);
	}

	@Override
	public void exitScalar(ICSSParser.ScalarContext ctx) {
		// idem
		ASTNode node = currentContainer.pop();
		currentContainer.peek().addChild(node);
	}

	@Override
	public void enterPixel(ICSSParser.PixelContext ctx) {
		// idem
		PixelLiteral pixel = new PixelLiteral(ctx.PIXELSIZE().getText());
		currentContainer.push(pixel);
	}

	@Override
	public void exitPixel(ICSSParser.PixelContext ctx) {
		// idem
		ASTNode pixel = currentContainer.pop();
		currentContainer.peek().addChild(pixel);
	}

	@Override
	public void enterPercentage(ICSSParser.PercentageContext ctx) {
		// idem
		PercentageLiteral percentage = new PercentageLiteral(ctx.PERCENTAGE().getText());
		currentContainer.push(percentage);
	}

	@Override
	public void exitPercentage(ICSSParser.PercentageContext ctx) {
		// idem
		ASTNode percentage = currentContainer.pop();
		currentContainer.peek().addChild(percentage);
	}

	@Override
	public void enterColor(ICSSParser.ColorContext ctx) {
		// idem
		ColorLiteral color = new ColorLiteral(ctx.COLOR().getText());
		currentContainer.push(color);
	}

	@Override
	public void exitColor(ICSSParser.ColorContext ctx) {
		// idem
		ASTNode color = currentContainer.pop();
		currentContainer.peek().addChild(color);
	}

	@Override
	public void enterBoolean(ICSSParser.BooleanContext ctx) {
		// idem
		BoolLiteral bool = new BoolLiteral(ctx.TRUE() != null);
		currentContainer.push(bool);
	}

	@Override
	public void exitBoolean(ICSSParser.BooleanContext ctx) {
		// idem
		ASTNode bool = currentContainer.pop();
		currentContainer.peek().addChild(bool);
	}

	@Override
	public void enterMultiply(ICSSParser.MultiplyContext ctx) {
		// idem
		MultiplyOperation mult = new MultiplyOperation();
		currentContainer.push(mult);
	}

	@Override
	public void exitMultiply(ICSSParser.MultiplyContext ctx) {
		// idem
		ASTNode mult = currentContainer.pop();
		currentContainer.peek().addChild(mult);
	}

	@Override
	public void enterSum(ICSSParser.SumContext ctx) {
		// idem
		AddOperation sum = new AddOperation();
		currentContainer.push(sum);
	}

	@Override
	public void exitSum(ICSSParser.SumContext ctx) {
		// idem
		ASTNode sum = currentContainer.pop();
		currentContainer.peek().addChild(sum);
	}

	@Override
	public void enterSub(ICSSParser.SubContext ctx) {
		// idem
		SubtractOperation sub = new SubtractOperation();
		currentContainer.push(sub);
	}

	@Override
	public void exitSub(ICSSParser.SubContext ctx) {
		// idem
		ASTNode sub = currentContainer.pop();
		currentContainer.peek().addChild(sub);
	}
}
