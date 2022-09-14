
package edu.ufl.cise.plpfa22;

/**
 * 
 * @author Guangze Wang
 * STATE:
 * 0 -- START
 * 1 -- INT_0
 * 2 -- INT_LIT
 */
public class MLexer implements ILexer {

    private String s, tmp;
    private int columnNum, lineNum, sPos, startPos, cnt;
    private int state;
    
    private static String reservedWords[] = {
    	"CONST", "VAR", "PROCEDURE", "CALL", "BEGIN", "END", "IF", "THEN", "WHILE", "DO"
    };
    
    private static String booleanWords[] = {
    		"TRUE", "FALSE"
    };
    
    MLexer (String _input) {
        s = _input;
        lineNum = 1;
        columnNum = 1;
        sPos = 0;
        state = 0;    // START state
    }
    
    @Override    // for testing
    public void showString() {
    	System.out.println(s);
    }

	@Override
	public IToken next() throws LexicalException {
		boolean isDone = false;
		IToken tokenNow = new MToken(34);
		state = 0; cnt = 0;
		if (sPos >= s.length()) { return tokenNow; }   // return EOF
		char n = s.charAt(sPos);
		while (!isDone) {
			switch (state) {
			case 0:                                               // START state
				startPos = sPos;
				cnt = 0;
				if (n == '0') { 
					state = 1; cnt++; 
				}
				else if (isNoneZeroDigit(n)) { 
					state = 2; cnt++; 
				}
				else if (n == ' ') {
					sPos++; columnNum++; n = s.charAt(sPos); 
				}
				else if (n == '\n' || n == 'r') {
					if (n == '\r' & sPos + 1 < s.length() & s.charAt(sPos+1) == '\n') {
						sPos++;
					}
					sPos++; columnNum = 1; lineNum++; n = s.charAt(sPos);
				}
				else if (n == '.' || n == ',' || n == '(' || n == ')' || n == '+' || n == '-' || n == '*' || 						
						 n == '/' || n == '%' || n == '?' || n == '!' || n == ':' || n == '=' || n == '#' ||
					     n == '<' || n == '>'){
					System.out.println("haha");
					String tmp = s.substring(sPos, sPos+1);
					cnt++;
					if (sPos + 1 < s.length()) {
						char nn = s.charAt(sPos + 1);
						if ((n == ':' || n == '<' || n == '>') & nn == '=') {
							tmp = s.substring(sPos, sPos+2); 
							cnt++;
						}
					}
					tokenNow = getRestToken(tmp, lineNum, columnNum);
					sPos = sPos + cnt;
					columnNum = columnNum + cnt;
					isDone = true;
				}
				break;
				
			case 1:                                               // INT_0 state
				//System.out.println("The cnt is:"+cnt);
				tokenNow = new MToken(1, s.substring(startPos, startPos+cnt), lineNum, columnNum);
				sPos = startPos + cnt;
				columnNum = columnNum + cnt;
				state = 0; 
				isDone = true;
				break;
				
			case 2:                                               // INT_LIT state
				n = s.charAt(startPos+cnt);
				if (n == '0' || isNoneZeroDigit(n)) {
					cnt++;
					if (startPos+cnt >= s.length()) {
						tokenNow = new MToken(1, s.substring(startPos, startPos+cnt), lineNum, columnNum);
						isDone = true;
					}
				}
				else {
					//System.out.println("here"+s.substring(startPos, startPos+cnt));
					tokenNow = new MToken(1, s.substring(startPos, startPos+cnt), lineNum, columnNum);
					sPos = startPos + cnt;
					columnNum = columnNum + cnt;
					state = 0; 
					isDone = true;
				}
				break;
			}
		}
		return tokenNow;
	}

	@Override
	public IToken peek() throws LexicalException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean isNoneZeroDigit(char n) {
		if (n > '0' & n <= '9') return true;
		return false;
	}
    
	private IToken getRestToken(String a,int b, int c) throws LexicalException{
		IToken tokenNow;
		System.out.println(a);
		switch (a) {
		case ".":
			tokenNow = new MToken(4, a, b, c);
			break;
		case ",":
			tokenNow = new MToken(5, a, b, c);
			break;
		default:
			throw new LexicalException("Program logic error");
		}
		
		return tokenNow;
	}
	
}
