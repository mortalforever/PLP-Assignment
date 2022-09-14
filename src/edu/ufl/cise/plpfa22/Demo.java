package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.CompilerComponentFactory;
import edu.ufl.cise.plpfa22.ILexer;
import edu.ufl.cise.plpfa22.MLexer;
import edu.ufl.cise.plpfa22.IToken;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.LexicalException;
import edu.ufl.cise.plpfa22.LexerTest;

public class Demo {

	public static void main(String[] args) {
		System.out.println("11");
		String init = " 123 TRUE Tfedw \" ";
		ILexer lexer = new MLexer(init);
		lexer.showString();
	}
}
