
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
		//return s.substring(1, s.length()-1);
		if (num_kind == 2) {
			String a = s.substring(1, s.length()-1);
			String b = "";
			for (int i = 0; i < a.length(); i++) {
				//System.out.println(i);
				//System.out.println(a.length());
				if (a.charAt(i) != '\\') {
						b = b.concat(a.substring(i,i+1));
				}
				else {
					i++;
					switch (a.charAt(i)) {
						case 'b':
							b = b + '\b';
							break;
						case 't':
							b = b + '\t';
							break;
						case 'n':
							b = b + '\n';
							break;
						case 'f':
							b = b + '\f';
							break;
						case 'r':
							b = b + '\r';
							break;
						case '\\':
							b = b + '\\';
							break;
						case '\"':
							b = b + '\"';
							break;
						case '\'':
							b = b + '\'';
							break;

					}
				}
			}
			return b;
		}
		else return s;
	}
    
}
