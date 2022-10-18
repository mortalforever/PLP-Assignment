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
		List<ConstDec> c = block.constDecs;
		for (int i = 0; i < c.size(); i++) {
			ConstDec nc = c.get(i);
			
		}
		
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws PLPException {
		return program.block.visit(this, arg);
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIdent(Ident ident, Object arg) throws PLPException {
		// TODO Auto-generated method stub
		return null;
	}

}
