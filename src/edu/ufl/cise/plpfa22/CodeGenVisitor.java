package edu.ufl.cise.plpfa22;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
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

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	final String packageName;
	final String className;
	final String sourceFileName;
	final String fullyQualifiedClassName; 
	final String classDesc;
	
	ClassWriter classWriter;

	
	public  CodeGenVisitor(String className, String packageName, String sourceFileName) {
		super();
		this.packageName = packageName;
		this.className = className;
		this.sourceFileName = sourceFileName;
		this.fullyQualifiedClassName = packageName + "/" + className;
		this.classDesc="L"+this.fullyQualifiedClassName+';';
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws PLPException {
		MethodVisitor methodVisitor = (MethodVisitor)arg;
		methodVisitor.visitCode();
		for (ConstDec constDec : block.constDecs) {
			constDec.visit(this, null);
		}
		for (VarDec varDec : block.varDecs) {
			varDec.visit(this, methodVisitor);
		}
		for (ProcDec procDec: block.procedureDecs) {
			procDec.visit(this, null);
		}
		//add instructions from statement to method
		block.statement.visit(this, arg);
		methodVisitor.visitInsn(RETURN);
		methodVisitor.visitMaxs(0,0);
		methodVisitor.visitEnd();
		return null;

	}

	@Override
	public Object visitProgram(Program program, Object arg) throws PLPException {
		//create a classWriter and visit it
		classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		//Hint:  if you get failures in the visitMaxs, try creating a ClassWriter with 0
		// instead of ClassWriter.COMPUTE_FRAMES.  The result will not be a valid classfile,
		// but you will be able to print it so you can see the instructions.  After fixing,
		// restore ClassWriter.COMPUTE_FRAMES
		classWriter.visit(V18, ACC_PUBLIC | ACC_SUPER, fullyQualifiedClassName, null, "java/lang/Object", null);

		//get a method visitor for the main method.		
		MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		//visit the block, passing it the methodVisitor
		program.block.visit(this, methodVisitor);
		//finish up the class
        classWriter.visitEnd();
        //return the bytes making up the classfile
		return classWriter.toByteArray();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		statementOutput.expression.visit(this, arg);
		Type etype = statementOutput.expression.getType();
		String JVMType = (etype.equals(Type.NUMBER) ? "I" : (etype.equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));
		String printlnSig = "(" + JVMType +")V";
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", printlnSig, false);
		return null;
	}

	@Override
	public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
		for (Statement statement: statementBlock.statements) {
			statement.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		statementIf.expression.visit(this, arg);
		statementIf.statement.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		Type argType = expressionBinary.e0.getType();
		Kind op = expressionBinary.op.getKind();
		switch (argType) {
		case NUMBER -> {
			expressionBinary.e0.visit(this, arg);
			expressionBinary.e1.visit(this, arg);
			switch (op) {
				case PLUS -> mv.visitInsn(IADD);
				case MINUS -> mv.visitInsn(ISUB);
				case TIMES -> mv.visitInsn(IMUL);
				case DIV -> mv.visitInsn(IDIV);
				case MOD -> mv.visitInsn(IREM);
				case EQ -> {
					Label labelNumEqFalseBr = new Label();
					mv.visitJumpInsn(IF_ICMPNE, labelNumEqFalseBr);
					mv.visitInsn(ICONST_1);
					Label labelPostNumEq = new Label();
					mv.visitJumpInsn(GOTO, labelPostNumEq);
					mv.visitLabel(labelNumEqFalseBr);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostNumEq);
				}
				case NEQ -> {
					Label labelNumNeq = new Label();
					mv.visitJumpInsn(IF_ICMPEQ, labelNumNeq);
					mv.visitInsn(ICONST_1);
					Label labelPostNumNeq = new Label();
					mv.visitJumpInsn(GOTO, labelPostNumNeq);
					mv.visitLabel(labelNumNeq);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostNumNeq);
				}
				case LT -> {
					Label labelNumLt = new Label();
					mv.visitJumpInsn(IF_ICMPGE, labelNumLt);
					mv.visitInsn(ICONST_1);
					Label labelPostNumLt = new Label();
					mv.visitJumpInsn(GOTO, labelPostNumLt);
					mv.visitLabel(labelNumLt);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostNumLt);
				}
				case LE -> {
					Label labelNumLe = new Label();
					mv.visitJumpInsn(IF_ICMPGT, labelNumLe);
					mv.visitInsn(ICONST_1);
					Label labelPostNumLe = new Label();
					mv.visitJumpInsn(GOTO, labelPostNumLe);
					mv.visitLabel(labelNumLe);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostNumLe);
				}
				case GT -> {
					Label labelNumGt = new Label();
					mv.visitJumpInsn(IF_ICMPLE, labelNumGt);
					mv.visitInsn(ICONST_1);
					Label labelPostNumGt = new Label();
					mv.visitJumpInsn(GOTO, labelPostNumGt);
					mv.visitLabel(labelNumGt);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostNumGt);
				}	
				case GE -> {
					Label labelNumGe = new Label();
					mv.visitJumpInsn(IF_ICMPLT, labelNumGe);
					mv.visitInsn(ICONST_1);
					Label labelPostNumGe = new Label();
					mv.visitJumpInsn(GOTO, labelPostNumGe);
					mv.visitLabel(labelNumGe);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostNumGe);
				}
				default -> {
					throw new IllegalStateException("code gen bug in visitExpressionBinary NUMBER");
				}
			}
			;
		}
		case BOOLEAN -> {
			expressionBinary.e0.visit(this, arg);
			expressionBinary.e1.visit(this, arg);
			switch (op) {
				case PLUS -> mv.visitInsn(IOR);
				case TIMES -> mv.visitInsn(IAND);
				case EQ -> {
					Label labelBoolEqFalseBr = new Label();
					mv.visitJumpInsn(IF_ICMPNE, labelBoolEqFalseBr);
					mv.visitInsn(ICONST_1);
					Label labelPostBoolEq = new Label();
					mv.visitJumpInsn(GOTO, labelPostBoolEq);
					mv.visitLabel(labelBoolEqFalseBr);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostBoolEq);
				}
				case NEQ -> {
					Label labelBoolNeq = new Label();
					mv.visitJumpInsn(IF_ICMPEQ, labelBoolNeq);
					mv.visitInsn(ICONST_1);
					Label labelPostBoolNeq = new Label();
					mv.visitJumpInsn(GOTO, labelPostBoolNeq);
					mv.visitLabel(labelBoolNeq);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostBoolNeq);
				}
				case LT -> {
					Label labelBoolLt = new Label();
					mv.visitJumpInsn(IF_ICMPGE, labelBoolLt);
					mv.visitInsn(ICONST_1);
					Label labelPostBoolLt = new Label();
					mv.visitJumpInsn(GOTO, labelPostBoolLt);
					mv.visitLabel(labelBoolLt);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostBoolLt);
				}
				case LE -> {
					Label labelBoolLe = new Label();
					mv.visitJumpInsn(IF_ICMPGT, labelBoolLe);
					mv.visitInsn(ICONST_1);
					Label labelPostBoolLe = new Label();
					mv.visitJumpInsn(GOTO, labelPostBoolLe);
					mv.visitLabel(labelBoolLe);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostBoolLe);
				}
				case GT -> {
					Label labelBoolGt = new Label();
					mv.visitJumpInsn(IF_ICMPLE, labelBoolGt);
					mv.visitInsn(ICONST_1);
					Label labelPostBoolGt = new Label();
					mv.visitJumpInsn(GOTO, labelPostBoolGt);
					mv.visitLabel(labelBoolGt);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostBoolGt);
				}	
				case GE -> {
					Label labelBoolGe = new Label();
					mv.visitJumpInsn(IF_ICMPLT, labelBoolGe);
					mv.visitInsn(ICONST_1);
					Label labelPostBoolGe = new Label();
					mv.visitJumpInsn(GOTO, labelPostBoolGe);
					mv.visitLabel(labelBoolGe);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostBoolGe);
				}
				default -> {
					throw new IllegalStateException("code gen bug in visitExpressionBinary BOOLEAN");
				}
			}
			;
		}
		case STRING -> {
			expressionBinary.e0.visit(this, arg);
			expressionBinary.e1.visit(this, arg);
			switch (op) {
				case PLUS -> mv.visitMethodInsn(INVOKESTATIC, this.className, "myConcat", "("+"Ljava/lang/String;"+"Ljava/lang/String;"+")"+"Ljava/lang/String;",false);
				case EQ -> {
					Label labelStrEqFalseBr = new Label();
					mv.visitJumpInsn(IF_ACMPNE, labelStrEqFalseBr);
					mv.visitInsn(ICONST_1);
					Label labelPostStrEq = new Label();
					mv.visitJumpInsn(GOTO, labelPostStrEq);
					mv.visitLabel(labelStrEqFalseBr);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostStrEq);
				}
				case NEQ -> {
					Label labelStrNeq = new Label();
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
					mv.visitInsn(ICONST_1);
					Label labelPostStrNeq = new Label();
					mv.visitJumpInsn(GOTO, labelPostStrNeq);
					mv.visitLabel(labelStrNeq);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostStrNeq);
				}
				case LT -> {
					Label labelStrLt = new Label();
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
					mv.visitInsn(ICONST_1);
					Label labelPostStrLt = new Label();
					mv.visitJumpInsn(GOTO, labelPostStrLt);
					mv.visitLabel(labelStrLt);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostStrLt);
				}
				case LE -> {
					Label labelStrLe = new Label();
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
					mv.visitInsn(ICONST_1);
					Label labelPostStrLe = new Label();
					mv.visitJumpInsn(GOTO, labelPostStrLe);
					mv.visitLabel(labelStrLe);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostStrLe);
				}
				case GT -> {
					Label labelStrGt = new Label();
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
					mv.visitInsn(ICONST_1);
					Label labelPostStrGt = new Label();
					mv.visitJumpInsn(GOTO, labelPostStrGt);
					mv.visitLabel(labelStrGt);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostStrGt);
				}	
				case GE -> {
					Label labelStrGe = new Label();
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
					mv.visitInsn(ICONST_1);
					Label labelPostStrGe = new Label();
					mv.visitJumpInsn(GOTO, labelPostStrGe);
					mv.visitLabel(labelStrGe);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostStrGe);
				}
				default -> {
					throw new IllegalStateException("code gen bug in visitExpressionBinary STRING");
				}
			}
			;
		}
		default -> {
			throw new IllegalStateException("code gen bug in visitExpressionBinary");
		}
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitLdcInsn(expressionNumLit.getFirstToken().getIntValue());
		return null;
	}

	@Override
	public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitLdcInsn(expressionStringLit.getFirstToken().getStringValue());
		return null;
	}

	@Override
	public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitLdcInsn(expressionBooleanLit.getFirstToken().getStringValue());
		return null;
	}

	@Override
	public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
		return null;
	}

	@Override
	public Object visitIdent(Ident ident, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

}
