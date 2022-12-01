package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.ASTNode;
import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Declaration;
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
import edu.ufl.cise.plpfa22.ast.Types;
import edu.ufl.cise.plpfa22.ast.Types.Type;
import edu.ufl.cise.plpfa22.ast.VarDec;
import edu.ufl.cise.plpfa22.IToken.Kind;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


public class TypeASTVisitor implements ASTVisitor{
	
	boolean fullytyped = true, typechanges = false;
	
	int tNest;
	HashMap<String, List<Declaration> > scopenode;
	
	TypeASTVisitor () {
		tNest = -1;
		scopenode = new HashMap<String, List<Declaration> >();
		fullytyped = true;
		typechanges = false;
	}
	

	@Override
	public Object visitProgram(Program program, Object arg) throws PLPException {
		while (true) {
			fullytyped = true; typechanges = false;
			program.block.visit(this, arg);
			if (fullytyped) return null;
			if (!fullytyped & !typechanges) {
				//System.out.println(fullytyped);
				//System.out.println(typechanges);
				throw new TypeCheckException("not fully typed");
				//return null;
			}
		}
	}
	
	@Override
	public Object visitBlock(Block block, Object arg) throws PLPException {
		tNest++;
		List<ConstDec> c = block.constDecs;
		for (int i = 0; i < c.size(); i++) {
			c.get(i).visit(this, arg);
		}
		List<VarDec> v = block.varDecs;
		for (int i = 0; i < v.size(); i++) {
			v.get(i).visit(this, arg);
		}
		List<ProcDec> p = block.procedureDecs;
		for (int i = 0; i < p.size(); i++) {
			
			String s = p.get(i).ident.getStringValue();
			if (scopenode.containsKey(s)) {
				List<Declaration> d = scopenode.get(s);
				for (int j = 0; j < d.size(); j++) {
					if (d.get(j).getNest() == tNest) {
						throw new ScopeException("ident defined twice");
					} 
				}
				ProcDec cd = p.get(i);
				cd.setNest(tNest);
				scopenode.get(s).add(cd);
			}
			else {
				List<Declaration> d = new ArrayList<Declaration>();
				ProcDec cd = p.get(i);
				cd.setNest(tNest);
				d.add(cd);
				scopenode.put(s, d);
			}
		}
		for (int i = 0; i < p.size(); i++) {
			p.get(i).visit(this, arg);
		}
		
		block.statement.visit(this, arg);
		
		for (int i = 0; i < c.size(); i++) {
			String s = c.get(i).ident.getStringValue();
			scopenode.get(s).remove(scopenode.get(s).size()-1);
			if (scopenode.get(s).size() == 0) {
				scopenode.remove(s);
			}
		}
		for (int i = 0; i < v.size(); i++) {
			String s = v.get(i).ident.getStringValue();
			scopenode.get(s).remove(scopenode.get(s).size()-1);
			if (scopenode.get(s).size() == 0) {
				scopenode.remove(s);
			}
		}
		for (int i = 0; i < p.size(); i++) {
			String s = p.get(i).ident.getStringValue();
			scopenode.get(s).remove(scopenode.get(s).size()-1);
			if (scopenode.get(s).size() == 0 ) {
				scopenode.remove(s);
			}
		}
		
		tNest--;
		return null;
	}
	
	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
		String s = varDec.ident.getStringValue();
		if (scopenode.containsKey(s)) {
			List<Declaration> d = scopenode.get(s);
			for (int i = 0; i < d.size(); i++) {
				if (d.get(i).getNest() == tNest) {
					throw new ScopeException("ident defined twice");
				} 
			}
			VarDec cd = varDec;
			cd.setNest(tNest);
			scopenode.get(s).add(cd);
		}
		else {
			List<Declaration> d = new ArrayList<Declaration>();
			VarDec cd = varDec;
			cd.setNest(tNest);
			d.add(cd);
			scopenode.put(s, d);
		}
		return null;
	}

	@Override
	public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
		String s = constDec.ident.getStringValue();
		if (scopenode.containsKey(s)) {
			List<Declaration> d = scopenode.get(s);
			for (int i = 0; i < d.size(); i++) {
				if (d.get(i).getNest() == tNest) {
					throw new ScopeException("ident defined twice");
				} 
			}
			ConstDec cd = constDec;
			cd.setNest(tNest);
			scopenode.get(s).add(cd);
		}
		else {
			List<Declaration> d = new ArrayList<Declaration>();
			ConstDec cd = constDec;
			cd.setNest(tNest);
			d.add(cd);
			scopenode.put(s, d);
		}
		return null;
	}

	@Override
	public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
		procDec.setType(Type.PROCEDURE);
		procDec.block.visit(this, arg);
		return null;
	}
	
	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
		statementAssign.ident.visit(this, arg);
		statementAssign.expression.visit(this, arg);
		if (statementAssign.ident.getDec().getClass() == ConstDec.class) {
			throw new TypeCheckException("cannot assign to constant");
		}
		if (statementAssign.expression.getType() == null & statementAssign.ident.getDec().getType() == null) {
			fullytyped = false;
			//System.out.println("assign");
		}
		else if (statementAssign.ident.getDec().getType() == null & statementAssign.expression.getType() != null) {
			statementAssign.ident.getDec().setType(statementAssign.expression.getType());
			typechanges = true;
			statementAssign.ident.visit(this, arg);
		}
		else if (statementAssign.ident.getDec().getType() != null & statementAssign.expression.getType() == null) {
			statementAssign.expression.setType(statementAssign.ident.getDec().getType());
			typechanges = true;
			statementAssign.expression.visit(this, arg);
		}
		if (statementAssign.ident.getDec().getType() != statementAssign.expression.getType()) {
			throw new TypeCheckException("Wrong assign type");
		}
		return null;
	}

	@Override
	public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
		statementCall.ident.visit(this, arg);
		if (statementCall.ident.getDec().getType() == null) {
			fullytyped = false;
			//System.out.println("call");
		}
		else if (statementCall.ident.getDec().getType() != Type.PROCEDURE) {
			throw new TypeCheckException("ident in statementcall is not procedure");
		}
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
		statementInput.ident.visit(this, arg);
		if ((statementInput.ident.getDec()).getClass() == ConstDec.class) {
			throw new TypeCheckException("Can not Modify const variable");
		}
		if (statementInput.ident.getDec().getType() == Type.PROCEDURE) {
			throw new TypeCheckException("Cannot input procedure");
		}
		return null;
	}

	@Override
	public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
		statementOutput.expression.visit(this, arg);
		if (statementOutput.expression.getType() == null) {
			fullytyped = false;
			//System.out.println("output");
		}
		else {
			if (statementOutput.expression.getType() == Type.PROCEDURE) {
				throw new TypeCheckException("Cannot output procedure");
			}
		}
		return null;
	}

	@Override
	public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
		List<Statement> s = statementBlock.statements;
		for (int i = 0; i < s.size(); i++) {
			s.get(i).visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
		statementIf.expression.visit(this, arg);
		statementIf.statement.visit(this, arg);
		if (statementIf.expression.getType() == null) {
			fullytyped = false;
			//System.out.println("if");
		}
		else {
			if (statementIf.expression.getType() != Type.BOOLEAN) {
				throw new TypeCheckException("Expression in If have wrong type");
			}
		}
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
		statementWhile.expression.visit(this, arg);
		statementWhile.statement.visit(this, arg);
		if (statementWhile.expression.getType() == null) {
			fullytyped = false;
			//System.out.println("while");
		}
		else {
			if (statementWhile.expression.getType() != Type.BOOLEAN) {
				throw new TypeCheckException("Expression in While have wrong type");
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
		expressionBinary.e0.visit(this, arg);
		expressionBinary.e1.visit(this, arg);
		if (expressionBinary.e0.getType() == null & expressionBinary.e1.getType() == null) {
			if (expressionBinary.getType() == null ) {
				fullytyped = false;
				//System.out.println("expressionbinary");
			}
			else {
				Type t = expressionBinary.getType();
				IToken op = expressionBinary.op;
				expressionBinary.e0.setType(t);
				expressionBinary.e0.visit(this, arg);
				expressionBinary.e1.setType(t);;
				expressionBinary.e1.visit(this, arg);
				typechanges = true;
			}
		}
		if (expressionBinary.e0.getType() != null & expressionBinary.e1.getType() == null) {
			Type t = expressionBinary.e0.getType();
			expressionBinary.e1.setType(t);
			expressionBinary.e1.visit(this, arg);
			typechanges = true;
		}
		if (expressionBinary.e0.getType() == null & expressionBinary.e1.getType() != null) {
			Type t = expressionBinary.e1.getType();
			expressionBinary.e0.setType(t);
			expressionBinary.e0.visit(this,arg);
			typechanges = true;
		}
		if (expressionBinary.e0.getType() != null & expressionBinary.e1.getType() != null) {
			if (expressionBinary.e0.getType() != expressionBinary.e1.getType()) {
				throw new TypeCheckException("ExpressionBinary e0, e1 different type error");
			}
			IToken op = expressionBinary.op;
			Kind k = op.getKind();
			Type t = expressionBinary.e0.getType();
			if (k == Kind.PLUS) {
				if (t == Type.PROCEDURE) {
					throw new TypeCheckException("Procedure type ident in expression plus");
				}
				else {
					if (expressionBinary.getType() == null) typechanges = true;
					expressionBinary.setType(t);
				}
			}
			if (k == Kind.MINUS || k == Kind.DIV || k == Kind.MOD) {
				if (t == Type.NUMBER) {
					if (expressionBinary.getType() == null) typechanges = true;
					expressionBinary.setType(t);
				}
				else {
					throw new TypeCheckException("Not Number type in expression minus/div/mod");
				}
			}
			if (k == Kind.TIMES) {
				if (t == Type.NUMBER || t == Type.BOOLEAN) {
					if (expressionBinary.getType() == null) typechanges = true;
					expressionBinary.setType(t);
				}
				else {
					throw new TypeCheckException("Not Number or Boolean type in expression times");
				}
			}
			if (k == Kind.EQ || k == Kind.NEQ || k == Kind.LT || k == Kind.LE || k == Kind.GT || k == Kind.GE) {
				if (t == Type.PROCEDURE) {
					throw new TypeCheckException("Procedure type ident in expression ...");
				}
				else {
					if (expressionBinary.getType() == null) typechanges = true;
					expressionBinary.setType(Type.BOOLEAN);
				}
			}
		}
		
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
		IToken t = expressionIdent.getFirstToken();
		String s = t.getStringValue();
		if (!scopenode.containsKey(s)) {
			throw new ScopeException("not defined ident");
		}
		else {
			Declaration d =scopenode.get(s).get(scopenode.get(s).size()-1);
			if (expressionIdent.getType() == null) {
				if (d.getType() != null) { 
					expressionIdent.setType(d.getType());
					typechanges = true;
				}
				else { fullytyped = false;
					//System.out.println("expressionIdent");
				}
			}
			else {
				if (d.getType() != null) {
					if (d.getType() != expressionIdent.getType()) throw new TypeCheckException("ExpressionIdent type error");
				}
				else {
					d.setType(expressionIdent.getType());
					typechanges = true;
				}
			}
			expressionIdent.setNest(tNest);
			expressionIdent.setDec(d);
		}
		return null;
	}

	@Override
	public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
		if (expressionNumLit.getType() == null) {
			expressionNumLit.setType(Type.NUMBER);
			typechanges = true;
		}
		return null;
	}

	@Override
	public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
		if (expressionStringLit.getType() == null) {
			expressionStringLit.setType(Type.STRING);
			typechanges = true;
		}
		return null;
	}

	@Override
	public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
		if (expressionBooleanLit.getType() == null) {
			expressionBooleanLit.setType(Type.BOOLEAN);
			typechanges = true;
		}
		return null;
	}

	@Override
	public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
		return null;
	}

	@Override
	public Object visitIdent(Ident ident, Object arg) throws PLPException {
		IToken t = ident.getFirstToken();
		String s = t.getStringValue();
		if (!scopenode.containsKey(s)) {
			throw new ScopeException("not defined ident");
		}
		else {
			Declaration d =scopenode.get(s).get(scopenode.get(s).size()-1);
			ident.setDec(d);
			ident.setNest(tNest);
			if (d.getType() == null) { 
				fullytyped = false;
				//System.out.println("ident");
			}
		}
		return null;
	}

	
}
