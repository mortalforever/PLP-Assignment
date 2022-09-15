
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
		boolean isEnd = false;
		boolean endComment = true;
		IToken tokenNow = new MToken(34);
		state = 0; cnt = 0;
		if (sPos >= s.length()) { return tokenNow; }   // return EOF
		char n = s.charAt(sPos);
		while (!isDone) {
			switch (state) {
			case 0:                                    // START state
				startPos = sPos;
				cnt = 0;
				if (sPos >= s.length()) { isDone = true; break; }
				if (n == '0') { 
					state = 1; cnt++; 
					break;
				}
				else if (isNoneZeroDigit(n)) { 
					state = 2; cnt++; 
					break;
				}
				else if (n == ' ' || n == '\t') {
					sPos++; columnNum++; n = s.charAt(sPos);
					break;
				}
				else if (n == '\n' || n == 'r') {
					if (n == '\r' & sPos + 1 < s.length() & s.charAt(sPos+1) == '\n') {
						sPos++;
					}
					sPos++; columnNum = 1; lineNum++; n = s.charAt(sPos);
					break;
				}
				else if (n == '.' || n == ',' || n == '(' || n == ')' || n == '+' || n == '-' || n == '*' || 						
						 n == '/' || n == '%' || n == '?' || n == '!' || n == ':' || n == '=' || n == '#' ||
					     n == '<' || n == '>'){
					String tmp = s.substring(sPos, sPos+1);
					cnt++;
					if (sPos + 1 < s.length()) {
						char nn = s.charAt(sPos + 1);
						if ((n == ':' || n == '<' || n == '>') & nn == '=') {
							tmp = s.substring(sPos, sPos+2); 
							cnt++;
						}
						if (n == '/' & nn == '/') {
							state = 3; cnt++; endComment = false; break;
						}
					}
					tokenNow = getRestToken(tmp, lineNum, columnNum);
					sPos = sPos + cnt;
					columnNum = columnNum + cnt;
					isDone = true;
					break;
				}
				else if (n == '\"') {
					state = 4; isEnd = false;
					break;
				}
				else if (isFirstIdentChar(n)) {
					
				}
				state = 100;
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
						if (cnt > 10) {
							throw new LexicalException("This is a big int");
						}
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
				
			case 3:
				//System.out.println("here");
				while (!endComment) {
					if (sPos+cnt >= s.length()) { isDone = true; }
					char nxt = s.charAt(sPos+cnt);
					if (nxt != '\n' & nxt != 'r') {
						cnt++;
					}
					else if (nxt == '\n'){
						state = 0;
						sPos = sPos + cnt + 1;
						columnNum = 1;
						lineNum++;
						endComment = true;
					}
				}
				//isDone = true;
				break;
				
			case 4:
				while (!isEnd) {
					cnt++;
					//System.out.println(cnt);
					if (s.charAt(sPos+cnt) == '\"') {
						cnt++;
						isEnd = true;
						//System.out.println(isEnd);
						tokenNow = new MToken(2, s.substring(startPos, startPos+cnt), lineNum, columnNum);
						sPos = startPos + cnt;
						columnNum = columnNum + cnt;
						cnt = 0; 
						isDone = true;
						state = 0;
					}
					else if (s.charAt(sPos+cnt) == '\\') {
						cnt++;
						char nxt = s.charAt(sPos+cnt);
						if (nxt == '\\' || nxt == 'b' || nxt == 't' || nxt == 'n' ||
							nxt == '\"' || nxt == 'r' || nxt == '\'' ) {
							cnt++;
						}
						else {
							throw new LexicalException("Wrong input String value");
						}
					}
					/**        Can I have line breaks in a string? Reserved
					else {
						
					}
					**/
				}
				break;
				
			default:
				throw new LexicalException("Wrong input value");
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
	
	private boolean isFirstIdentChar(char n) {
		if ((n >= 'a' & n <= 'z') || (n >= 'A' & n <= 'Z') ||n == '_' || n == '$') return true;
		return false;
	}
	
	private boolean isIdentChar(char n) {
		if ((n >= '0' & n <= '9') || (n >= 'a' & n <= 'z') || (n >= 'A' & n <= 'Z') ||
			 n == '_' || n == '$') return true;
		return false;
	}
    
	private IToken getRestToken(String a,int b, int c) throws LexicalException{
		IToken tokenNow;
		//System.out.println(a);
		switch (a) {
		case ".":
			tokenNow = new MToken(4, a, b, c);
			break;
		case ",":
			tokenNow = new MToken(5, a, b, c);
			break;
		case ";":
			tokenNow = new MToken(6, a, b, c);
			break;
		case "\"":
			tokenNow = new MToken(7, a, b, c);
			break;
		case "(":
			tokenNow = new MToken(8, a, b, c);
			break;
		case ")":
			tokenNow = new MToken(9, a, b, c);
			break;
		case "+":
			tokenNow = new MToken(10, a, b, c);
			break;
		case "-":
			tokenNow = new MToken(11, a, b, c);
			break;
		case "*":
			tokenNow = new MToken(12, a, b, c);
			break;
		case "/":
			tokenNow = new MToken(13, a, b, c);
			break;
		case "%":
			tokenNow = new MToken(14, a, b, c);
			break;
		case "?":
			tokenNow = new MToken(15, a, b, c);
			break;
		case "!":
			tokenNow = new MToken(16, a, b, c);
			break;
		case ":=":
			tokenNow = new MToken(17, a, b, c);
			break;
		case "=":
			tokenNow = new MToken(18, a, b, c);
			break;
		case "#":
			tokenNow = new MToken(19, a, b, c);
			break;
		case "<":
			tokenNow = new MToken(20, a, b, c);
			break;
		case "<=":
			tokenNow = new MToken(21, a, b, c);
			break;
		case ">":
			tokenNow = new MToken(22, a, b, c);
			break;
		case ">=":
			tokenNow = new MToken(23, a, b, c);
			break;
		default:
			throw new LexicalException("Program logic error");
		}
		
		return tokenNow;
	}
	
}
