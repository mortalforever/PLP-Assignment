package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.CompilerComponentFactory;
import edu.ufl.cise.plpfa22.ILexer;
import edu.ufl.cise.plpfa22.MLexer;
import edu.ufl.cise.plpfa22.IParser;
import edu.ufl.cise.plpfa22.MParser;
import edu.ufl.cise.plpfa22.IToken;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.ASTNode;
import edu.ufl.cise.plpfa22.ast.Types;
import edu.ufl.cise.plpfa22.LexicalException;
import edu.ufl.cise.plpfa22.LexerTest;
import java.util.List;
import java.util.Vector;

public class Demo {

	public static void main(String[] args) throws PLPException {
		//String init = ""\"\\b \\t \\n \\f \\r \"";
		
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
		/**
		String init = "\"\\b \\t \\n \\f \\r \"";
		//String init = """
		//			  "\"\\b \\t \\n \\f \\r \""
		//		      """; 
		ILexer lexer = new MLexer(init);
		lexer.showString();
		IToken token = lexer.peek();
		token = lexer.peek();
		//token = lexer.next();
		//token = lexer.next();
		//token = lexer.next();
		//token = lexer.next();
		//token = lexer.next();
		//token = lexer.next();
		System.out.println(token.getKind());
		System.out.println(Types.Type.BOOLEAN);
		String a = "STRING_LIT";
		System.out.println(IToken.Kind.valueOf(a)==token.getKind());
		//System.out.println(token.getIntValue());
		System.out.println(token.getStringValue());
		System.out.println(token.getSourceLocation());
		**/
		/**
		List<String> a = new Vector<String>();
		System.out.println(a.size());
		a.add(null);
		System.out.println(a.size());
		a.add("111");
		System.out.println(a.size());
		**/
		String input = """
				CONST a=1,b=2;
				CONST c = 3 ;
				VAR d, e;
				PROCEDURE a; CONST f =1;;
				.""";
		/**
		
		ILexer lexer = new MLexer(input);
		IToken token = lexer.next();
		token = lexer.next();
		token = lexer.next();
		token = lexer.next();
		token = lexer.next();
		//System.out.println(token.getIntValue());
		System.out.println(token.getStringValue());
		System.out.println(token.getSourceLocation());
		**/
		MParser parser = new MParser(new MLexer(input));
		ASTNode ast = parser.parse();
		System.out.println(ast.toString());
		//System.out.println(ast.getFirstToken().getKind());
	}
}
