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
import edu.ufl.cise.plpfa22.ast.VarDec;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


public class MASTVisitor implements ASTVisitor{

	int tNest;
	HashMap<String, List<Declaration> > scopenode;
	
	MASTVisitor () {
		tNest = -1;
		scopenode = new HashMap<String, List<Declaration> >();
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
	public Object visitProgram(Program program, Object arg) throws PLPException {
		return program.block.visit(this, arg);
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
		statementAssign.ident.visit(this, arg);
		statementAssign.expression.visit(this, arg);
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
	public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
		statementCall.ident.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
		statementInput.ident.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
		statementOutput.expression.visit(this, arg);
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
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
		statementWhile.expression.visit(this, arg);
		statementWhile.statement.visit(this, arg);
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
		expressionBinary.e0.visit(this, arg);
		expressionBinary.e1.visit(this, arg);
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
			expressionIdent.setDec(scopenode.get(s).get(scopenode.get(s).size()-1));
		}
		return null;
	}

	@Override
	public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
		return null;
	}

	@Override
	public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
		return null;
	}

	@Override
	public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
		return null;
	}

	@Override
	public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
		procDec.block.visit(this, arg);
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
			ident.setDec(scopenode.get(s).get(scopenode.get(s).size()-1));
		}
		return null;
	}

}
