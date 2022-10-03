
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
		return s.toCharArray();
	}

	@Override
	public SourceLocation getSourceLocation() {
		return tokenPos;
	}

	@Override
	public int getIntValue() {
		return Integer.valueOf(s);
	}

	@Override
	public boolean getBooleanValue() {
		boolean n = true;
		switch (s) {
			case "TRUE":
				n = true; 
				break;
			case "FALSE":
				n = false;
				break;
		}
		return n;
		
	}

	@Override
	public String getStringValue() {
		return s;
	}
    
}