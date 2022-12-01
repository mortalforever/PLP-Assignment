package edu.ufl.cise.plpfa22;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
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
import edu.ufl.cise.plpfa22.CodeGenUtils.GenClass;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	final String packageName;
	final String className;
	final String sourceFileName;
	final String fullyQualifiedClassName; 
	final String classDesc;
	
	ClassWriter classWriter;
	List<ClassWriter> cwlist;
	List<GenClass> genclass;
	List<String> nowFullyClassName; 
	List<String> nowClassName;
	int nowNest;
	
	public static final String stringClass = "java/lang/String";
	public static final String stringDesc = "Ljava/lang/String;";
	public static final String listClass = "java/util/ArrayList";
	public static final String listDesc = "Ljava/util/ArrayList;";
	
	public  CodeGenVisitor(String className, String packageName, String sourceFileName) {
		super();
		this.packageName = packageName;
		this.className = className;
		this.sourceFileName = sourceFileName;
		this.fullyQualifiedClassName = packageName + "/" + className;
		//this.fullyQualifiedClassName = className + "/" + packageName;
		this.classDesc="L"+this.fullyQualifiedClassName+';';
		this.genclass = new ArrayList<GenClass>();
		this.nowFullyClassName = new ArrayList<String>();
		this.cwlist = new ArrayList<ClassWriter>();
		this.nowClassName = new ArrayList<String>();
		nowClassName.add(className);
		nowNest = -1;
	}
	
	@Override
	public Object visitProgram(Program program, Object arg) throws PLPException {
		//create a classWriter and visit it
		nowFullyClassName.add(fullyQualifiedClassName);
		
		classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		classWriter.visit(V18, ACC_PUBLIC | ACC_SUPER, fullyQualifiedClassName, null, "java/lang/Object", new String[] {"java/lang/Runnable"});   
		cwlist.add(classWriter);
		
		
		if (sourceFileName != null) cwlist.get(0).visitSource(sourceFileName, null);
		//classWriter.visitSource(sourceFileName, null);
		// init method
		MethodVisitor initVisitor = cwlist.get(0).visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		initVisitor.visitCode();
		initVisitor.visitVarInsn(ALOAD, 0);
		initVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		initVisitor.visitInsn(RETURN);
		initVisitor.visitMaxs(0, 0);
		initVisitor.visitEnd();
		
		// main method
		MethodVisitor mainVisitor = cwlist.get(0).visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		mainVisitor.visitCode();
		mainVisitor.visitTypeInsn(NEW, fullyQualifiedClassName);
		mainVisitor.visitInsn(DUP);
		mainVisitor.visitMethodInsn(INVOKESPECIAL, fullyQualifiedClassName, "<init>", "()V", false);
		mainVisitor.visitMethodInsn(INVOKEVIRTUAL, fullyQualifiedClassName, "run", "()V", false);
		mainVisitor.visitInsn(RETURN);
		mainVisitor.visitMaxs(0, 0);
		mainVisitor.visitEnd();
		
		program.block.visit(this, cwlist.get(0));
		
		cwlist.get(0).visitEnd();
        genclass.add(new GenClass(fullyQualifiedClassName,cwlist.get(0).toByteArray()));
        cwlist.remove(0);
        List<GenClass> answer = new ArrayList<GenClass>();
        for (int i = genclass.size()-1; i >= 0; i--) {
        	answer.add(genclass.get(i));
        }
		return answer;
	}
	
	@Override
	public Object visitBlock(Block block, Object arg) throws PLPException {
		nowNest++;
		//MethodVisitor methodVisitor = (ClassWriter)arg.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		ClassWriter cw = (ClassWriter)arg;
		MethodVisitor methodVisitor = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		methodVisitor.visitCode();
		for (ConstDec constDec : block.constDecs) {
			constDec.visit(this, null);
		}
		for (VarDec varDec : block.varDecs) {
			varDec.visit(this, arg);
		}
		for (ProcDec procDec: block.procedureDecs) {
			procDec.visit(this, null);
		}
		//add instructions from statement to method
		
		block.statement.visit(this, methodVisitor);
		
		methodVisitor.visitInsn(RETURN);
		methodVisitor.visitMaxs(0,0);
		methodVisitor.visitEnd();
		nowNest--;
		return null;

	}
	/**
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
	**/

	@Override
	public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
		return null;
	}
	
	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
		ClassWriter cw = (ClassWriter)arg;
		Type etype = varDec.getType();
		if (etype != null ) {
			String JVMType = (etype.equals(Type.NUMBER) ? "I" : (etype.equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));
			FieldVisitor fieldVisitor = cw.visitField(ACC_PUBLIC, varDec.ident.getStringValue(),JVMType, null, null);
			fieldVisitor.visitEnd();
		}
		return null;
	}
	
	@Override
	public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
		String newClassName = nowFullyClassName.get(nowNest) + "$" + procDec.ident.getStringValue();
		
		nowFullyClassName.add(newClassName);
		nowClassName.add(procDec.ident.getStringValue());
		cwlist.get(nowNest).visitInnerClass(newClassName, nowFullyClassName.get(nowNest), nowClassName.get(nowNest+1),0);
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cw.visit(V18, ACC_PUBLIC | ACC_SUPER, newClassName, null, "java/lang/Object", new String[] {"java/lang/Runnable"});
		cwlist.add(cw);
		if (sourceFileName != null) cwlist.get(nowNest+1).visitSource(sourceFileName, null);
		
		// innerclass and nesthost, nestmember
		cwlist.get(0).visitNestMember(newClassName);
		cwlist.get(nowNest+1).visitNestHost(fullyQualifiedClassName);
		for (int i = 0; i < nowNest; i++) {
			cwlist.get(nowNest+1).visitInnerClass(nowFullyClassName.get(i+1), nowFullyClassName.get(i), nowClassName.get(i+1), 0);
		}
		
		//this$n
		FieldVisitor fieldVisitor = cwlist.get(nowNest+1).visitField(ACC_FINAL | ACC_SYNTHETIC, "this$" + nowNest, "L"+nowFullyClassName.get(nowNest)+";", null, null);
		fieldVisitor.visitEnd();
		
		// init method
		MethodVisitor initVisitor = cwlist.get(nowNest+1).visitMethod(ACC_PUBLIC, "<init>", "(L"+nowFullyClassName.get(nowNest)+";)V", null, null);
		initVisitor.visitCode();
		initVisitor.visitVarInsn(ALOAD, 0);
		initVisitor.visitVarInsn(ALOAD, 1);
		initVisitor.visitFieldInsn(PUTFIELD, nowFullyClassName.get(nowNest+1), "this$"+nowNest, "L"+nowFullyClassName.get(nowNest)+";");
		initVisitor.visitVarInsn(ALOAD, 0);
		initVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		initVisitor.visitInsn(RETURN);
		initVisitor.visitMaxs(0, 0);
		initVisitor.visitEnd();
		
		procDec.block.visit(this, cwlist.get(nowNest+1));
		
		cwlist.get(nowNest+1).visitEnd();
		genclass.add(new GenClass(newClassName,cwlist.get(nowNest+1).toByteArray()));
	    cwlist.remove(nowNest+1);
	    nowClassName.remove(nowNest+1);
	    nowFullyClassName.remove(nowNest+1);
		return null;
	}
	
	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		statementAssign.expression.visit(this, arg);
		statementAssign.ident.visit(this, arg);
		Type etype = statementAssign.ident.getDec().getType();
		String JVMType = (etype.equals(Type.NUMBER) ? "I" : (etype.equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));
		int identNest = statementAssign.ident.getDec().getNest();
		String identName = statementAssign.ident.getFirstToken().getStringValue();
		mv.visitInsn(SWAP);
		mv.visitFieldInsn(PUTFIELD, nowFullyClassName.get(identNest), identName, JVMType);
		return null;
	}

	@Override
	public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
		int callNest = statementCall.ident.getDec().getNest();
		String callClassName = nowFullyClassName.get(callNest);
		String callName = statementCall.ident.getFirstToken().getStringValue();
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitTypeInsn(NEW, callClassName+"$"+callName);
		mv.visitInsn(DUP);
		//mv.visitVarInsn(ALOAD, 0);
		statementCall.ident.visit(this, arg);
		mv.visitMethodInsn(INVOKESPECIAL, callClassName+"$"+callName, "<init>", "(L"+callClassName+";)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, callClassName+"$"+callName, "run", "()V", false);
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitTypeInsn(NEW, "java/util/Scanner");
		mv.visitInsn(DUP);
		mv.visitFieldInsn(GETSTATIC, "java/lang/System.in", ":", "Ljava/io/InputStream;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);
		mv.visitVarInsn(ASTORE, 1);
		statementInput.ident.visit(this, arg);
		mv.visitVarInsn(ALOAD, 1);
		Type etype = statementInput.ident.getDec().getType();
		String identName = statementInput.ident.getFirstToken().getStringValue();
		String JVMType = (etype.equals(Type.NUMBER) ? "I" : (etype.equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));
		int identNest = statementInput.ident.getDec().getNest();
		if (etype.equals(Type.NUMBER)) {
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "<init>", JVMType, false);
		}
		else {
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "<init>", JVMType, false);
		}
		mv.visitFieldInsn(PUTFIELD, nowFullyClassName.get(identNest), identName, JVMType);
		return null;
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
		Label l1 = new Label();
		mv.visitLdcInsn(true);
		mv.visitJumpInsn(IF_ICMPNE, l1);
		Label l2 = new Label();
		mv.visitJumpInsn(GOTO, l2);
		mv.visitLabel(l1);
		statementIf.statement.visit(this, arg);
		mv.visitLabel(l2);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		Label l1 = new Label();
		mv.visitJumpInsn(GOTO, l1);
		Label l2 = new Label();
		mv.visitLabel(l2);
		statementWhile.statement.visit(this, arg);
		mv.visitLabel(l1);
		statementWhile.expression.visit(this, arg);
		mv.visitLdcInsn(true);
		mv.visitJumpInsn(IF_ICMPNE, l2);
		return null;
	}

	@Override
	public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
		return null;
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
				case PLUS -> mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;",false);
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
					//mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
					mv.visitJumpInsn(IF_ACMPNE,  labelStrNeq);
					mv.visitInsn(ICONST_1);
					Label labelPostStrNeq = new Label();
					mv.visitJumpInsn(GOTO, labelPostStrNeq);
					mv.visitLabel(labelStrNeq);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostStrNeq);
				}
				case LT -> {
					mv.visitInsn(SWAP);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
				}
				case LE -> {
					mv.visitInsn(SWAP);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
				}
				case GT -> {
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
				}	
				case GE -> {
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
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
		MethodVisitor mv = (MethodVisitor)arg;
		if (expressionIdent.getDec().getClass() == ConstDec.class) {
			mv.visitLdcInsn(((ConstDec)expressionIdent.getDec()).val);
		}
		else {
			int identNest = expressionIdent.getDec().getNest();
			String identName = expressionIdent.getFirstToken().getStringValue();
			Type etype = expressionIdent.getDec().getType();
			String JVMType = (etype.equals(Type.NUMBER) ? "I" : (etype.equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));
			mv.visitVarInsn(ALOAD, 0);
			if (identNest < nowNest) {
				mv.visitFieldInsn(GETFIELD, nowFullyClassName.get(nowNest), "this$"+(nowNest-1), "L"+nowFullyClassName.get(nowNest-1)+";");
				for (int i = nowNest - 1; i > identNest; i--) {
					mv.visitFieldInsn(GETFIELD, nowFullyClassName.get(i), "this$"+(i-1), "L"+nowFullyClassName.get(i-1)+";");
				}
			}
			mv.visitFieldInsn(GETFIELD, nowFullyClassName.get(identNest), identName, JVMType);
		}
		return null;
	}

	@Override
	public Object visitIdent(Ident ident, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		int identNest = ident.getDec().getNest();
		String identName = ident.getFirstToken().getStringValue();
		Type etype = ident.getDec().getType();
		String JVMType = (etype.equals(Type.NUMBER) ? "I" : (etype.equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));
		mv.visitVarInsn(ALOAD, 0);
		if (identNest < nowNest) {
			//System.out.println("this$"+(nowNest-1));
			mv.visitFieldInsn(GETFIELD, nowFullyClassName.get(nowNest), "this$"+(nowNest-1), "L"+nowFullyClassName.get(nowNest-1)+";");
			for (int i = nowNest - 1; i > identNest; i--) {
				//System.out.println(i);
				mv.visitFieldInsn(GETFIELD, nowFullyClassName.get(i), "this$"+(i-1), "L"+nowFullyClassName.get(i-1)+";");
			}
		}
	
		return null;
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
		mv.visitLdcInsn(expressionBooleanLit.getFirstToken().getBooleanValue());
		return null;
	}


}
