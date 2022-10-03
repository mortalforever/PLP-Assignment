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
import java.util.ArrayList;

import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.ASTNode;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Expression;
import edu.ufl.cise.plpfa22.ast.ExpressionBinary;
import edu.ufl.cise.plpfa22.ast.ExpressionBooleanLit;
import edu.ufl.cise.plpfa22.ast.ExpressionIdent;
import edu.ufl.cise.plpfa22.ast.ExpressionNumLit;
import edu.ufl.cise.plpfa22.ast.ExpressionStringLit;
import edu.ufl.cise.plpfa22.ast.Ident;
import edu.ufl.cise.plpfa22.ast.ProcDec;
import edu.ufl.cise.plpfa22.ast.Program;
import edu.ufl.cise.plpfa22.ast.Statement;
import edu.ufl.cise.plpfa22.ast.StatementAssign;
import edu.ufl.cise.plpfa22.ast.StatementBlock;
import edu.ufl.cise.plpfa22.ast.StatementCall;
import edu.ufl.cise.plpfa22.ast.StatementEmpty;
import edu.ufl.cise.plpfa22.ast.StatementIf;
import edu.ufl.cise.plpfa22.ast.StatementInput;
import edu.ufl.cise.plpfa22.ast.StatementOutput;
import edu.ufl.cise.plpfa22.ast.StatementWhile;
import edu.ufl.cise.plpfa22.ast.VarDec;

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
				CONST a=3;
				VAR x,y,z;
				PROCEDURE p;
				  VAR j;
				  BEGIN
				     ? x;
				     IF x = 0 THEN ! y ;
				     WHILE j < 24 DO CALL z
				  END;
				! z
				.
				""";
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
		Block v0 = ((Program) ast).block;
		List<ConstDec> v1 = ((Block) v0).constDecs;
		System.out.println(v1.size());
		IToken v2 = ((ConstDec) v1.get(0)).ident;
		System.out.println(String.valueOf(v2.getText()));
		Integer v3 = (Integer) ((ConstDec) v1.get(0)).val;
		System.out.println(v3);
		List<VarDec> v4 = ((Block) v0).varDecs;
		System.out.println(v4.size());
		IToken v5 = ((VarDec) v4.get(0)).ident;
		System.out.println(v5.getText());
		List<ProcDec> v8 = ((Block) v0).procedureDecs;
		IToken v9 = ((ProcDec) v8.get(0)).ident;
		System.out.println(v9.getText());
		System.out.println(ast.toString());
		//System.out.println(ast.getFirstToken().getKind());
	}
}
