
package edu.ufl.cise.plpfa22;

public class MLexer implements ILexer {

    String s;
    int pos, lineNum;

    MLexer (String _input) {
        s = _input;
        pos = 1;
        lineNum = 1;

    }
    
    @Override
    public void showString() {
    	System.out.println(s);
    }

	@Override
	public IToken next() throws LexicalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IToken peek() throws LexicalException {
		// TODO Auto-generated method stub
		return null;
	}

    
}
