package com.dedasys.hecl;

import java.lang.*;
import java.util.*;

/**
 * Describe class <code>Parse</code> here.
 *
 * @author <a href="mailto:davidw@dedasys.com">David N. Welton</a>
 * @version 1.0
 */

public class Parse {
    protected Vector outList;
    protected ParseState state;
    protected Interp interp = null;
    protected String in;
    protected Thing currentOut;

    protected boolean parselist = false;

    /**
     * The <code>more</code> method returns a boolean value indicating
     * whether there is more text to be parsed or not.
     *
     * @return a <code>boolean</code> value
     */
    public boolean more() {
	return !state.eof;
    }

    /**
     * Creates a new <code>Parse</code> instance.  Not actually used
     * by anything.
     *
     */
    public Parse() {
    }

    /**
     * Creates a new <code>Parse</code> instance.
     *
     * @param interp_in a <code>Interp</code> value
     * @param in_in a <code>String</code> value
     */
    public Parse(Interp interp_in, String in_in) {
	interp = interp_in;
	in = in_in;
	state = new ParseState(in);
    }


    /**
     * The <code>parse</code> method runs the parser on the text added
     * by creating a new Parse instance.
     *
     * @return a <code>Vector</code> value
     * @exception HeclException if an error occurs
     */
    public Vector parse ()
	throws HeclException {
	outList = new Vector();
//	currentOut = new StringBuffer("");
	currentOut = new Thing("");
	state.eoc = false;

	//state.remaining();
	parseLine(in, state);
	if (outList.size() > 0 ) {
	    return outList;
	}
	return null;
    }

    /**
     * The <code>addCurrent</code> method adds a new element to the command parsed.
     *
     */
    protected void addCurrent() {
	outList.add(currentOut);
	currentOut = new Thing("");
    }

    /**
     * Describe <code>appendCurrent</code> method here.
     *
     */
    protected void appendCurrent() {
	Thing last;
	int sz = outList.size();

	addCurrent();
	last = (Thing)outList.elementAt(sz - 2);
	last.appendString(currentOut.toString());
	currentOut = new Thing("");
	outList.removeElementAt(sz - 1);
    }

    /**
     * Describe <code>addCurrent</code> method here.
     *
     * @param newthing a <code>Thing</code> value
     */
    public void addCurrent(Thing newthing) {
	outList.add(newthing);
	currentOut = new Thing("");
    }

    /**
     * Describe <code>addCommand</code> method here.
     *
     * @exception HeclException if an error occurs
     */
    public void addCommand() throws HeclException {
	Thing saveout = currentOut;
	currentOut = new Thing("");
	parseCommand(state);
	currentOut = new Thing(saveout.toString() + currentOut.toString());
    }

    /**
     * Describe <code>addDollar</code> method here.
     *
     * @param docopy a <code>boolean</code> value
     * @exception HeclException if an error occurs
     */
    public void addDollar(boolean docopy)
	throws HeclException {
	Thing saveout = currentOut;
	currentOut = new Thing("");
	parseDollar(state, docopy);
	currentOut = new Thing(saveout.toString() + currentOut.toString());
    }

    /**
     * Describe <code>parseLine</code> method here.
     *
     * @param in a <code>String</code> value
     * @param state a <code>ParseState</code> value
     * @exception HeclException if an error occurs
     */
    public void parseLine(String in, ParseState state)
	throws HeclException {
	char ch;

	while (true) {
	    ch = state.nextchar();
	    if (state.done()) {
		return;
	    }
//	    if (Character.getType(ch) == Character.LINE_SEPARATOR)
	    switch (ch) {
		case '{':
		    parseBlock(state);
		    addCurrent();
		    break;
		case '[':
		    parseCommand(state);
		    addCurrent();
		    break;
		case '$':
		    parseDollar(state, true);
		    addCurrent();
		    break;
		case '&':
		    parseDollar(state, false);
		    addCurrent();
		    break;
		case '"':
		    parseText(state);
		    addCurrent();
		    break;
		case ' ':
		    break;
		case '	':
		    break;
		case '#':
		    parseComment(state);
		    return;
		case '\n':
		    return;
		default:
		    currentOut.appendString(ch);
		    parseWord(state);
		    addCurrent();
		    break;
	    }
	}
    }

    /**
     * Describe <code>parseComment</code> method here.
     *
     * @param state a <code>ParseState</code> value
     */
    private void parseComment(ParseState state) {
	char ch;
	while (true) {
	    ch = state.nextchar();
	    if (ch == '\n' || state.done()) {
		return;
	    }
	}
    }

    /**
     * Describe <code>parseDollar</code> method here.
     *
     * @param state a <code>ParseState</code> value
     * @param docopy a <code>boolean</code> value
     * @exception HeclException if an error occurs
     */
    private void parseDollar(ParseState state, boolean docopy)
	throws HeclException {
	char ch;
	ch = state.nextchar();
	if (ch == '{') {
	    parseBlock(state);
	} else {
	    while (ch > 'A' && ch < 'z') {
		currentOut.appendString(ch);
		ch = state.nextchar();
	    }
	    if (!state.done()) {
		state.rewind();
	    }
	}
	try {
	    if (docopy) {
		currentOut = interp.getVar(currentOut.toString()).copy();
	    } else {
		currentOut = interp.getVar(currentOut.toString());
	    }
	} catch (HeclException e) {
	    e.where(outList.elementAt(0).toString());
	    throw e;
	}
    }

    /**
     * Describe <code>parseBlock</code> method here.
     *
     * @param state a <code>ParseState</code> value
     * @exception HeclException if an error occurs
     */
    public void parseBlock(ParseState state)
	throws HeclException {
	parseBlockOrCommand(state, true);
    }

    /**
     * Describe <code>parseCommand</code> method here.
     *
     * @param state a <code>ParseState</code> value
     * @exception HeclException if an error occurs
     */
    public void parseCommand(ParseState state)
	throws HeclException {
	parseBlockOrCommand(state, false);
    }

    /**
     * Describe <code>parseBlockOrCommand</code> method here.
     *
     * @param state a <code>ParseState</code> value
     * @param block a <code>boolean</code> value
     * @exception HeclException if an error occurs
     */
    public void parseBlockOrCommand(ParseState state,
					    boolean block)
	throws HeclException {
	int level = 1;
	char ldelim, rdelim;
	char ch;

	if (block == true) {
	    ldelim = '{';
	    rdelim = '}';
	} else {
	    ldelim = '[';
	    rdelim = ']';
	}

	while (true) {
	    ch = state.nextchar();
	    if (state.done()) {
		return;
	    }
	    if (ch == ldelim) {
		level ++;
	    } else if (ch == rdelim) {
		level --;
	    }
	    if (level == 0) {
		/* It's just a block, return it. */
		if (block || parselist) {
//		    return new Thing(out);
		    return;
		} else {
		    Eval eval = new Eval();
		    //System.out.println("TO EVAL: " + currentOut.toString());
		    currentOut = eval.eval(interp, currentOut);
		    return;
		}
	    } else {
		currentOut.appendString(ch);
	    }
	}
    }

    /**
     * Describe <code>parseText</code> method here.
     *
     * @param state a <code>ParseState</code> value
     * @exception HeclException if an error occurs
     */
    public void parseText(ParseState state)
	throws HeclException {
	char ch;
	while (true) {
	    ch = state.nextchar();
	    if (state.done()) {
		return;
	    }
	    switch (ch) {
		case '\\':
		    ch = state.nextchar();
		    if (state.done()) {
			return;
		    }
		    currentOut.appendString(ch);
		    break;
		case '[':
		    addCommand();
		    break;
		case '$':
		    addDollar(true);
		    break;
 		case '&':
		    addDollar(false);
		    break;
		case '"':
		    return;
		default:
		    currentOut.appendString(ch);
		    break;
	    }
	}
    }

    /**
     * Describe <code>parseWord</code> method here.
     *
     * @param state a <code>ParseState</code> value
     * @exception HeclException if an error occurs
     */
    public void parseWord(ParseState state)
	throws HeclException {
	char ch;
	while (true) {
	    ch = state.nextchar();
	    if (state.done()) {
		return;
	    }
	    switch(ch) {
		case '[':
		    addCommand();
		    break;
		case '$':
		    addDollar(true);
		    break;
		case '&':
		    addDollar(false);
		    break;
		case '"':
		    addCurrent();
		    parseText(state);
		    appendCurrent();
		    break;
		case ' ':
//		    return new Thing(out);
		    return;
		case '\n':
		    state.eoc = true;
//		    return new Thing(out);
		    return;
		case '\\':
		    ch = state.nextchar();
		    if (state.done()) {
			return;
		    }
		    currentOut.appendString(ch);
		    break;
		default:
		    currentOut.appendString(ch);
//		    out.appendString(state.chars[state.idx]);
		    break;
	    }
	}
    }

    /**
     * <code>ParseState</code> 
     *
     */
    public class ParseState {
	public int len;

	private char[] chars;
	private int idx;
	public boolean eof;
	public boolean eoc;

	/**
	 * Creates a new <code>ParseState</code> instance.
	 *
	 * @param in a <code>String</code> value
	 */
	public ParseState(String in) {
	    chars = in.toCharArray();
	    len = in.length();
	    idx = 0;
	    eof = false;
	    eoc = false;
	}

	/**
	 * Describe <code>nextchar</code> method here.
	 *
	 * @return a <code>char</code> value
	 */
	public char nextchar() {
	    char result;
	    if (eoc) {
		return (char)0;
	    }
	    if (idx >= len) {
		eoc = true;
		eof = true;
		return (char)0;
	    }
	    result = chars[idx];
	    idx ++;
	    return result;
	}

	/**
	 * Describe <code>done</code> method here.
	 *
	 * @return a <code>boolean</code> value
	 */
	public boolean done() {
	    return (eof || eoc) ? true : false;
	}

	/**
	 * Describe <code>remaining</code> method here.
	 *
	 */
	public void remaining() {
	    System.out.println("remaining:" + chars[idx]);
	}

	/**
	 * Describe <code>rewind</code> method here.
	 *
	 */
	public void rewind() {
	    idx --;
	}
    }
}
