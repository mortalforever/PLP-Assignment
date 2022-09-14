
package edu.ufl.cise.plpfa22;

public class MToken implements IToken{

	private int num_kind = 0;
	private String s;
	private SourceLocation tokenPos;

	MToken (int num){
		num_kind = num;
		s = "";
		tokenPos = new SourceLocation(1,1);
	}
	
	MToken (int num, int l, int c) {
		num_kind = num;
		s = "";
		tokenPos = new SourceLocation(l, c);
	}
	
	MToken (int num, String ts, int l, int c) {
		num_kind = num;
		s = ts;
		tokenPos = new SourceLocation(l, c);
	}

	public void setPos(int lineNum, int columnNum) {
		tokenPos = new SourceLocation(lineNum, columnNum);
	}
	
	@Override
	public Kind getKind() {
		return Kind.values()[num_kind];
	}

	@Override
	public char[] getText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SourceLocation getSourceLocation() {
		return tokenPos;
	}

	@Override
	public int getIntValue() {
		//System.out.println(s);
		return Integer.valueOf(s);
	}

	@Override
	public boolean getBooleanValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getStringValue() {
		// TODO Auto-generated method stub
		return null;
	}
    
}
