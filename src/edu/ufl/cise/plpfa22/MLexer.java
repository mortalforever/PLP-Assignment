
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
    	"CONST", "VAR", "PROCEDURE", "CALL", "BEGIN", "END", "IF", "THEN", "WHILE", "DO" , "TRUE", "FALSE"
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
		boolean endIdent = true;
		IToken tokenNow = new MToken(34);
		state = 0; cnt = 0;
		if (sPos >= s.length()) { return tokenNow; }   // return EOF
		char n = s.charAt(sPos);
		while (!isDone) {
			//System.out.println(n);
			//System.out.println("here");
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
					     n == '<' || n == '>' || n == ';'){
					String tmp = s.substring(sPos, sPos+1);
					//System.out.println(n);
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
					cnt = 0;
					state = 0;
					isDone = true;
					break;
				}
				else if (n == '\"') {
					state = 4; isEnd = false;
					break;
				}
				else if (isFirstIdentChar(n)) {
					state = 5; cnt = 1; endIdent = false;
					break;
				}
				state = 100;
				break;
				
			case 1:                                               // INT_0 state
				//System.out.println("The cnt is:"+cnt);
				tokenNow = new MToken(1, s.substring(startPos, startPos+cnt), lineNum, columnNum);
				sPos = startPos + cnt;
				columnNum = columnNum + cnt;
				state = 0; 
				cnt = 0;
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
					cnt = 0;
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
						//System.out.println("here");
						char nxt = s.charAt(sPos+cnt);
						if (nxt == '\\' || nxt == 'b' || nxt == 't' || nxt == 'n' ||
							nxt == '\"' || nxt == 'r' || nxt == '\'' || nxt == 'f') {
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
			
			case 5:
				//System.out.println("here");
				while (!endIdent) {
					if (isIdentChar(s.charAt(sPos+cnt))) { cnt++; }
					else {
						tokenNow = new MToken(0, s.substring(startPos, startPos+cnt), lineNum, columnNum);
						sPos = sPos + cnt;
						columnNum = columnNum + cnt;
						cnt = 0;
						state = 0;
						endIdent = true;
						isDone = true;
						break;
					}
					String tmp = s.substring(sPos, sPos+cnt);
					if (isReserved(tmp)) {
						tokenNow = getReservedToken(tmp, lineNum, columnNum);
						sPos = sPos + cnt;
						columnNum = columnNum + cnt;
						cnt = 0;
						state = 0;
						endIdent = true;
						isDone = true;
						break;
					}
				}
				break;
			
			default:
				throw new LexicalException("Wrong input value");
			}
			
		}
		return tokenNow;
	}

	@Override
	public IToken peek() throws LexicalException {    //Still have columnNum problem
		boolean isDone = false;
		boolean isEnd = false;
		boolean endComment = true;
		boolean endIdent = true;
		IToken tokenNow = new MToken(34);
		state = 0; cnt = 0;
		if (sPos >= s.length()) { return tokenNow; }   // return EOF
		char n = s.charAt(sPos);
		while (!isDone) {
			switch (state) {
			case 0:                                    // START state
				startPos = sPos;
				cnt = 0;
				if (startPos >= s.length()) { isDone = true; break; }
				if (n == '0') { 
					state = 1; cnt++; 
					break;
				}
				else if (isNoneZeroDigit(n)) { 
					state = 2; cnt++; 
					break;
				}
				else if (n == ' ' || n == '\t') {
					//sPos++; 
					startPos++;
					//columnNum++; 
					n = s.charAt(startPos);
					break;
				}
				else if (n == '\n' || n == 'r') {
					if (n == '\r' & startPos + 1 < s.length() & s.charAt(startPos+1) == '\n') {
						startPos++;
					}
					//sPos++; 
					//columnNum = 1; 
					//lineNum++; 
					n = s.charAt(startPos);
					break;
				}
				else if (n == '.' || n == ',' || n == '(' || n == ')' || n == '+' || n == '-' || n == '*' || 						
						 n == '/' || n == '%' || n == '?' || n == '!' || n == ':' || n == '=' || n == '#' ||
					     n == '<' || n == '>'){
					String tmp = s.substring(startPos, startPos+1);
					cnt++;
					if (startPos + 1 < s.length()) {
						char nn = s.charAt(startPos + 1);
						if ((n == ':' || n == '<' || n == '>') & nn == '=') {
							tmp = s.substring(startPos, startPos+2); 
							cnt++;
						}
						if (n == '/' & nn == '/') {
							state = 3; cnt++; endComment = false; break;
						}
					}
					tokenNow = getRestToken(tmp, lineNum, columnNum);
					//sPos = sPos + cnt;
					//columnNum = columnNum + cnt;
					isDone = true;
					break;
				}
				else if (n == '\"') {
					state = 4; isEnd = false;
					break;
				}
				else if (isFirstIdentChar(n)) {
					state = 5; cnt = 1; endIdent = false;
					break;
				}
				state = 100;
				break;
				
			case 1:                                               // INT_0 state
				//System.out.println("The cnt is:"+cnt);
				tokenNow = new MToken(1, s.substring(startPos, startPos+cnt), lineNum, columnNum);
				//sPos = startPos + cnt;
				//columnNum = columnNum + cnt;
				state = 0; 
				cnt = 0;
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
					//sPos = startPos + cnt;
					//columnNum = columnNum + cnt;
					state = 0; 
					cnt = 0;
					isDone = true;
				}
				break;
				
			case 3:
				//System.out.println("here");
				while (!endComment) {
					if (startPos+cnt >= s.length()) { isDone = true; }
					char nxt = s.charAt(startPos+cnt);
					if (nxt != '\n' & nxt != 'r') {
						cnt++;
					}
					else if (nxt == '\n'){
						state = 0;
						startPos = startPos + cnt + 1;
						//sPos = sPos + cnt + 1;
						//columnNum = 1;
						//lineNum++;
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
						//sPos = startPos + cnt;
						//columnNum = columnNum + cnt;
						cnt = 0; 
						isDone = true;
						state = 0;
					}
					else if (s.charAt(sPos+cnt) == '\\') {
						cnt++;
						//System.out.println("here");
						char nxt = s.charAt(sPos+cnt);
						if (nxt == '\\' || nxt == 'b' || nxt == 't' || nxt == 'n' ||
							nxt == '\"' || nxt == 'r' || nxt == '\'' || nxt == 'f') {
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
			
			case 5:
				//System.out.println("here");
				while (!endIdent) {
					if (isIdentChar(s.charAt(startPos+cnt))) { cnt++; }
					else {
						tokenNow = new MToken(0, s.substring(startPos, startPos+cnt), lineNum, columnNum);
						//sPos = sPos + cnt;
						cnt = 0;
						state = 0;
						//columnNum = columnNum + cnt;
						endIdent = true;
						isDone = true;
						break;
					}
					String tmp = s.substring(sPos, sPos+cnt);
					if (isReserved(tmp)) {
						tokenNow = getReservedToken(tmp, lineNum, columnNum);
						//sPos = sPos + cnt;
						cnt = 0;
						state = 0;
						//columnNum = columnNum + cnt;
						endIdent = true;
						isDone = true;
						break;
					}
				}
				break;
			
			default:
				throw new LexicalException("Wrong input value");
			}
			
		}
		return tokenNow;
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

	private boolean isReserved(String tmp2) {
		for (int i = 0; i < reservedWords.length; i++) {
			if (tmp2.equals(reservedWords[i])) { return true; }
		}
		return false;
	}
	
	private IToken getReservedToken(String a,int b, int c) throws LexicalException{ 
		IToken tokenNow;
		switch (a) {
		case "CONST":
			tokenNow = new MToken(24, a, b, c);
			break;
		case "VAR":
			tokenNow = new MToken(25, a, b, c);
			break;
		case "PROCEDURE":
			tokenNow = new MToken(26, a, b, c);
			break;
		case "CALL":
			tokenNow = new MToken(27, a, b, c);
			break;
		case "BEGIN":
			tokenNow = new MToken(28, a, b, c);
			break;
		case "END":
			tokenNow = new MToken(29, a, b, c);
			break;
		case "IF":
			tokenNow = new MToken(30, a, b, c);
			break;
		case "THEN":
			tokenNow = new MToken(31, a, b, c);
			break;
		case "WHILE":
			tokenNow = new MToken(32, a, b, c);
			break;
		case "DO":
			tokenNow = new MToken(33, a, b, c);
			break;
		case "TRUE":
			tokenNow = new MToken(3, a, b, c);
			break;
		case "FALSE":
			tokenNow = new MToken(3, a, b, c);
			break;
		default:
			throw new LexicalException("Program logic error in ReservedWords");
		}
		return tokenNow;
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
