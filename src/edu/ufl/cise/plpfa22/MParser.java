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
import edu.ufl.cise.plpfa22.ast.Types.Type;
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
			if (kind != kind.DOT) t = scanner.next();
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
			ConstDec c = new ConstDec(ident, ident, t.getIntValue());
			c.setType(Type.NUMBER);
			return c;
		}
		if (t.getKind() == Kind.STRING_LIT) {
			ConstDec c = new ConstDec(ident, ident, t.getStringValue());
			c.setType(Type.STRING);
			return c;
		}
		if (t.getKind() == Kind.BOOLEAN_LIT) {
			ConstDec c = new ConstDec(ident, ident, t.getBooleanValue());
			c.setType(Type.BOOLEAN);
			return c;
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
	
	private Expression getExpression(IToken first) throws PLPException {
		Expression tmp1 = null;
		tmp1 = getAddiExpression(first);
		while ((t.getKind() == Kind.LT) || (t.getKind() == Kind.LE)|| (t.getKind() == Kind.EQ) ||
				(t.getKind() == Kind.NEQ) || (t.getKind() == Kind.GT) || (t.getKind() == Kind.GE)) {
			if(t.getKind() == Kind.LT) {
				IToken op = t;
				match(Kind.LT);
				tmp1 = new ExpressionBinary(first, tmp1, op, getMultExpression(t));
			} 
			else if(t.getKind() == Kind.LE) {
				IToken op = t;
				match(Kind.LE);
				tmp1 = new ExpressionBinary(first, tmp1, op, getMultExpression(t));
			}
			else if(t.getKind() == Kind.EQ) {
				IToken op = t;
				match(Kind.EQ);
				tmp1 = new ExpressionBinary(first, tmp1, op, getMultExpression(t));
			} 
			else if(t.getKind() == Kind.NEQ) {
				IToken op = t;
				match(Kind.NEQ);
				tmp1 = new ExpressionBinary(first, tmp1, op, getMultExpression(t));
			} 
			else if(t.getKind() == Kind.GT) {
				IToken op = t;
				match(Kind.GT);
				tmp1 = new ExpressionBinary(first, tmp1, op, getMultExpression(t));
			} 
			else if(t.getKind() == Kind.GE) {
				IToken op = t;
				match(Kind.GE);
				tmp1 = new ExpressionBinary(first, tmp1, op, getMultExpression(t));
			} 
		}
		if (tmp1 != null) return tmp1;
		throw new SyntaxException("Wrong structure in Expression");
	}
	
	private Expression getAddiExpression(IToken first) throws PLPException {
		Expression tmp1 = null;
		tmp1 = getMultExpression(first);
		while ((t.getKind() == Kind.PLUS) || (t.getKind() == Kind.MINUS)) {
			if(t.getKind() == Kind.PLUS) {
				IToken op = t;
				match(Kind.PLUS);
				tmp1 = new ExpressionBinary(first, tmp1, op, getMultExpression(t));
			} 
			else if(t.getKind() == Kind.MINUS) {
				IToken op = t;
				match(Kind.MINUS);
				tmp1 = new ExpressionBinary(first, tmp1, op, getMultExpression(t));
			} 
		}
		if (tmp1 != null) return tmp1;
		throw new SyntaxException("Wrong structure in Expression");
	}

	private Expression getMultExpression(IToken first) throws PLPException {
		Expression tmp1 = null;
		tmp1 = getPrimExpression(first);
		while ((t.getKind() == Kind.TIMES) || (t.getKind() == Kind.DIV) || (t.getKind() == Kind.MOD)) {
			if(t.getKind() == Kind.TIMES) {
				IToken op = t;
				match(Kind.TIMES);
				tmp1 = new ExpressionBinary(first, tmp1, op, getPrimExpression(t));
			} 
			else if(t.getKind() == Kind.DIV) {
				IToken op = t;
				match(Kind.DIV);
				tmp1 = new ExpressionBinary(first, tmp1, op, getPrimExpression(t));
			} 
			else if(t.getKind() == Kind.MOD) {
				IToken op = t;
				match(Kind.MOD);
				tmp1 = new ExpressionBinary(first, tmp1, op, getPrimExpression(t));
			} 
		}
		if (tmp1 != null) return tmp1;
		throw new SyntaxException("Wrong structure in Expression");
	}

	private Expression getPrimExpression(IToken first) throws PLPException {
		if (first.getKind() == Kind.IDENT) {
			ExpressionIdent tmpIdent = new ExpressionIdent(t);
			match(Kind.IDENT);
			return tmpIdent;
		}
		else if (first.getKind() == Kind.NUM_LIT) {
			ExpressionNumLit tmpnumlit = new ExpressionNumLit(t);
			match(Kind.NUM_LIT);
			return tmpnumlit;
		}
		else if (first.getKind() == Kind.STRING_LIT) {
			ExpressionStringLit tmpstringlit = new ExpressionStringLit(t);
			match(Kind.STRING_LIT);
			return tmpstringlit;
		} 
		else if (first.getKind() == Kind.BOOLEAN_LIT) {
			ExpressionBooleanLit tmpbooleanlit = new ExpressionBooleanLit(t);
			match(Kind.BOOLEAN_LIT);
			return tmpbooleanlit;
		}
		else if (first.getKind() == Kind.LPAREN) {
			match(Kind.LPAREN);
			Expression tmp1 = getExpression(t);
			match(Kind.RPAREN);
			return tmp1;
		}
		throw new SyntaxException("Wrong Value Type in Expression");
	}

	private Statement getStatement(IToken first) throws PLPException{
		IToken tmp;
		if (t.getKind() == Kind.IDENT) {
			tmp = t;
			match(Kind.IDENT);
			match(Kind.ASSIGN);
			return new StatementAssign(first, new Ident(tmp), getExpression(t));
		}
		else if (t.getKind() == Kind.KW_CALL) {
			match(Kind.KW_CALL);
			if (t.getKind() != Kind.IDENT) {
				throw new SyntaxException("Wrong Ident name in Statement");
			} 
			tmp = t;
			match(Kind.IDENT);
			return new StatementCall(first, new Ident(tmp));
		}
		else if (t.getKind() == Kind.QUESTION) {
			match(Kind.QUESTION);
			if (t.getKind() != Kind.IDENT) {
				throw new SyntaxException("Wrong Ident name in Statement");
			} 
			tmp = t;
			match(Kind.IDENT);
			return new StatementInput(first, new Ident(tmp));
		}
		else if (t.getKind() == Kind.BANG) {
			match(Kind.BANG);
			return new StatementOutput(first, getExpression(t));
		}
		else if (t.getKind() == Kind.KW_BEGIN) {
			//System.out.println("here");
			match(Kind.KW_BEGIN);
			Statement statetmp;
			List<Statement> slist = new ArrayList<Statement>();
			statetmp = getStatement(t);
			slist.add(statetmp);
			while (t.getKind() == Kind.SEMI) {
				match(Kind.SEMI);
				statetmp = getStatement(t);
				slist.add(statetmp);
			}
			match(Kind.KW_END);
			return new StatementBlock(first, slist);
		}
		else if (t.getKind() == Kind.KW_IF) {
			match(Kind.KW_IF);
			Expression etmp = getExpression(t);
			match(Kind.KW_THEN);
			Statement statetmp = getStatement(t);
			return new StatementIf(first, etmp, statetmp);
		}
		else if (t.getKind() == Kind.KW_WHILE) {
			match(Kind.KW_WHILE);
			Expression etmp = getExpression(t);
			match(Kind.KW_DO);
			Statement statetmp = getStatement(t);
			return new StatementWhile(first, etmp, statetmp);
		}
		return new StatementEmpty(first);
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