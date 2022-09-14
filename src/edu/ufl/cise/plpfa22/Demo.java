package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.CompilerComponentFactory;
import edu.ufl.cise.plpfa22.ILexer;
import edu.ufl.cise.plpfa22.MLexer;
import edu.ufl.cise.plpfa22.IToken;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.LexicalException;
import edu.ufl.cise.plpfa22.LexerTest;

public class Demo {

	public static void main(String[] args) throws LexicalException {
		//String init = "123 \r TRUE Tfedw \"ssss\" ";
		
		/**
		String init = "0123 23411232 123224"; 
		ILexer lexer = new MLexer(init);
		//lexer.showString();
		IToken token = lexer.next();
		token = lexer.next();
		token = lexer.next();
		//token = lexer.next();
		System.out.println(token.getKind());
		System.out.println(token.getIntValue());
		System.out.println(token.getSourceLocation());
		//System.out.println(new String("123\"").length());
		 **/
		
		
		String init = """
					  .0123,
					  12	
				      """; 
		ILexer lexer = new MLexer(init);
		lexer.showString();
		IToken token = lexer.next();
		token = lexer.next();
		token = lexer.next();
		token = lexer.next();
		token = lexer.next();
		System.out.println(token.getKind());
		System.out.println(token.getIntValue());
		System.out.println(token.getSourceLocation());
	}
}
