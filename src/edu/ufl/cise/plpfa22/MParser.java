package edu.ufl.cise.plpfa22;

import java.util.ArrayList;
import java.util.List;

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

public class MParser implements IParser {

	ILexer scanner;
	IToken t;
	
	MParser (String _input) {
		scanner = new MLexer(_input);
	}
	
	MParser (ILexer lexer) throws PLPException {
		scanner = lexer;
		t = scanner.next();
	}
	
	IToken consume() throws LexicalException {
		t = scanner.next();
		return t;
	}
	
	void match(Kind kind) throws PLPException {
		if (t.getKind() == kind) {
			t = scanner.next();
		}
		else {
			throw new SyntaxException("Wrong Grammar");
		}
	}
	
	private ConstDec getConstDec() throws PLPException {
		IToken ident = t;
		match(Kind.IDENT);
		match(Kind.EQ);

		if (t.getKind() == Kind.NUM_LIT) {
			return new ConstDec(ident, ident, t.getIntValue());
		}
		if (t.getKind() == Kind.STRING_LIT) {
			return new ConstDec(ident, ident, t.getStringValue());
		}
		if (t.getKind() == Kind.BOOLEAN_LIT) {
			return new ConstDec(ident, ident, t.getBooleanValue());
		}
		throw new SyntaxException("Wrong Value Type in ConstDec");
	}
	
	private VarDec getVarDec() throws PLPException{
		IToken ident = t;
		if (t.getKind() == Kind.IDENT){
			match(Kind.IDENT);
			return new VarDec(ident, ident);
		}
		throw new SyntaxException("Wrong Ident name in VarDec");
	}
	
	private ProcDec getProcDec(IToken first) throws PLPException{
		match(Kind.KW_PROCEDURE);
		IToken ident = t;
		if (t.getKind() != Kind.IDENT) {
			throw new SyntaxException("Wrong Ident name in ProcDec");
		}
		match(Kind.IDENT);
		match(Kind.SEMI);
		Block tblock = getBlock();
		match(Kind.SEMI);
		return new ProcDec(first, ident, tblock);
	}
	
	private Statement getStatement(IToken first) throws PLPException{
		if (t.getKind() == Kind.IDENT) {
			
		}
		else if (t.getKind() == Kind.KW_CALL) {
			
		}
		else if (t.getKind() == Kind.QUESTION) {
			
		}
		return null;
	}
	
	private Expression getExpression(IToken first) throws PLPException {
		
		return null;
	}
	
	private Block getBlock() throws PLPException {
		List<ConstDec> NconstDecs = new ArrayList<ConstDec>();
		List<VarDec> NvarDecs = new ArrayList<VarDec>();
		List<ProcDec> NprocDecs = new ArrayList<ProcDec>();
		Statement Nstatement = null;
		IToken ft = t;
		//System.out.println("here");
		
		while (t.getKind() == Kind.KW_CONST) {
			match(Kind.KW_CONST);
			NconstDecs.add(getConstDec());
			t = scanner.next();
			while (t.getKind() == Kind.COMMA) {
				match(Kind.COMMA);
				NconstDecs.add(getConstDec());
				t = scanner.next();
			}
			//System.out.println(t.getKind());
			match(Kind.SEMI);
		}
		
		while (t.getKind() == Kind.KW_VAR) {
			match(Kind.KW_VAR);
			NvarDecs.add(getVarDec());
			while (t.getKind() == Kind.COMMA) {
				match(Kind.COMMA);
				NvarDecs.add(getVarDec());
			}
			match(Kind.SEMI);
		}
		
		while (t.getKind() == Kind.KW_PROCEDURE) {
			NprocDecs.add(getProcDec(t));
		}
		Nstatement = getStatement(t);
		return new Block(ft, NconstDecs, NvarDecs, NprocDecs, Nstatement);
	}

	



	@Override
	public ASTNode parse() throws PLPException {
		
		Program Nprog = null;
		Block Nblock = null;
		IToken ft = t;
		Nblock = getBlock();
		match(Kind.DOT);
		Nprog = new Program(ft, Nblock);
		return Nprog;
	}
	
}