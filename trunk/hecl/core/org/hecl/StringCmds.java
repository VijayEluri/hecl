/* Copyright 2006 David N. Welton

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.hecl;


/**
 * The <code>StringCmds</code> class groups together a number of
 * commands that operate on Hecl strings.
 *
 * @author <a href="mailto:davidw@dedasys.com">David N. Welton</a>
 * @version 1.0
 */
class StringCmds extends Operator {
    public static final int APPEND = 1;
    public static final int SLEN = 2;
    public static final int SINDEX = 3;
    public static final int STREQ = 4;
    public static final int STRNEQ = 5;


    public RealThing operate(int cmd, Interp interp, Thing[] argv) throws HeclException {
	String str = argv[1].toString();
	switch (cmd) {
	    case APPEND:
		/* The 'append' command. */
		Thing result = argv[1];

		StringBuffer sb = new StringBuffer(StringThing.get(result));
		for (int i = 2; i < argv.length; i++) {
		    sb.append(argv[i].toString());
		}
		result.setVal(new StringThing(sb));
		interp.setResult(result);
		break;
	    case SLEN:
		/* 'slen' command. */
		return new IntThing(str.length());

	    case SINDEX:
		/* 'sindex' command. */
		int idx = IntThing.get(argv[2]);
		try {
		    char chars[] = new char[1];
		    chars[0] = str.charAt(idx);
		    interp.setResult(new String(chars));
		} catch (StringIndexOutOfBoundsException e) {
		    interp.setResult("");
		}
		break;
	    case STREQ:
	    case STRNEQ:
		/* 'eq' and 'ne' commands. */
		int i = Compare.compareString(argv[1],argv[2]);
		if(cmd == STREQ) {
		    return i != 0 ? IntThing.ZERO : IntThing.ONE;
		}
		return i != 0 ? IntThing.ONE : IntThing.ZERO;
	    default:
		throw new HeclException("Unknown string command '"
					+ argv[0].toString() + "' with code '"
					+ cmd + "'.");
	}
	return null;
    }


    public static void load(Interp ip) throws HeclException {
	Operator.load(ip);
    }


    public static void unload(Interp ip) throws HeclException {
	Operator.unload(ip);
    }


    protected StringCmds(int cmdcode,int minargs,int maxargs) {
	super(cmdcode, minargs, maxargs);
    }

    static {
        cmdtable.put("append", new StringCmds(APPEND,1,-1));
        cmdtable.put("slen", new StringCmds(SLEN,1,1));
        cmdtable.put("sindex", new StringCmds(SINDEX,2,2));
        cmdtable.put("eq", new StringCmds(STREQ,2,2));
	cmdtable.put("ne", new StringCmds(STRNEQ,2,2));
    }
}
