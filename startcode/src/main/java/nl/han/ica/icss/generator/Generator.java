package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;

public class Generator {

	public String generate(AST ast) {
		StringBuilder css2 = new StringBuilder();

		//
		for (ASTNode node : ast.root.getChildren()) {
			if(node instanceof Stylerule) {
				css2.append(generateProperty((Stylerule) node));
			}
		}
        return css2.toString();
	}

	// Function to generate the CSS string for a given Stylerule
	private String generateProperty(Stylerule stylerule) {
		StringBuilder styleruleStringBuilder = new StringBuilder();

		// Start the CSS block with the selector
		styleruleStringBuilder.append(stylerule.selectors.get(0)).append(" {\n");

		// Iterate over the declarations in the Stylerule
		for(ASTNode node : stylerule.getChildren()) {
			// Check if the node is a Declaration and append it to the CSS string
			if (node instanceof Declaration) {
				styleruleStringBuilder
						.append("  ")
						.append(((Declaration) node).property.name)
						.append(": ")
						.append(getLiteralValue((Literal) ((Declaration) node).expression))
						.append("\n");
			}
		}
		return styleruleStringBuilder.append("}\n").toString();
	}

	// Function to get the literal value of a given Literal
	private String getLiteralValue(Literal literal) {
		StringBuilder literalBuilder = new StringBuilder();

		// Check the type of the literal and return the corresponding value
		if (literal instanceof ColorLiteral){
			return literalBuilder.append(((ColorLiteral) literal).value).append(";").toString() ;
		} else if (literal instanceof PixelLiteral) {
			return literalBuilder.append(((PixelLiteral) literal).value).append("px;").toString() ;
		} else if (literal instanceof PercentageLiteral) {
			return literalBuilder.append(((PercentageLiteral) literal).value).append("%;").toString() ;
		}
		return "";
	}
}
