/* Copyright 2004 David N. Welton

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

package com.dedasys.hecl;

import java.util.*;

/**
 * The <code>CodeThing</code> class implements a chunk of "compiled"
 * code including multiple "Stanzas", or individual commands.
 *
 * @author <a href="mailto:davidw@dedasys.com">David N. Welton</a>
 * @version 1.0
 */

public class CodeThing {
    /* The number of lines of commands. */
    private Vector stanzas;

    CodeThing() {
	stanzas = new Vector();
    }

    /**
     * The <code>addStanza</code> method adds a new command and its
     * arguments.
     *
     * @param command <code>Command</code> value
     * @param argv <code>Thing[]</code> value
     */

    public void addStanza(Command command, Thing[] argv) {
	Stanza sz = new Stanza(command, argv);
	stanzas.addElement(sz);
	//System.out.println("ADDING : " + sz.toString() + "</ADDING>");
	// (new Throwable()).printStackTrace();
    }

    /**
     * The <code>run</code> method runs the CodeThing.
     *
     * @param interp <code>Interp</code> value
     * @exception HeclException if an error occurs
     */
    public void run(Interp interp) throws HeclException {

	//System.out.println("RUNNING: " + this.toString() + "</RUNNING>");
	for (Enumeration e = stanzas.elements(); e.hasMoreElements(); ) {
	    Stanza s = (Stanza)e.nextElement();
	    s.run(interp);
	}
    }

    /**
     * The <code>toString</code> method returns a String
     * representation of the commands it represents.
     *
     * @return a <code>String</code> value.
     */
    public String toString() {
	StringBuffer out = new StringBuffer();
	for (Enumeration e = stanzas.elements(); e.hasMoreElements(); ) {
	    Stanza s = (Stanza)e.nextElement();
	    out.append("[");
	    out.append(s.toString());
	    out.append("]\n");
	}
	return out.toString();
    }

    /**
     * The <code>Stanza</code> class represents one command.
     *
     * @author <a href="mailto:davidw@dedasys.com">David N. Welton</a>
     * @version 1.0
     */

    class Stanza {
	private Command command = null;
	private Thing[] argv = null;
	Eval eval = new Eval();

	Stanza(Command newcmd, Thing[] newargv) {
	    command = newcmd;
	    argv = newargv;
	}

	/**
	 * The <code>run</code> method runs the Stanza.
	 *
	 * @param interp <code>Interp</code> value
	 * @exception HeclException if an error occurs
	 */
	public void run(Interp interp) throws HeclException {
	    Thing[] newargv = new Thing[argv.length];

	    if (command == null) {
		String cmdName = argv[0].toString();
		/* FIXME - what if argv0 is $foo instead of foo? */
		command = interp.getCmd(cmdName);
		if (command == null) {
		    //(new Throwable()).printStackTrace();
		    throw new HeclException("Command " + cmdName + " does not exist");
		}
	    }

	    /* Create new array.  Run args that are SUBST or GROUP
	     * types, use references to others. */
	    for (int i = 0; i < argv.length; i ++) {
		if (argv[i].type == Thing.SUBST) {
		    /* If it's a SUBST, we eval it before passing off
		     * the results. */
		    eval.eval(interp, argv[i]);
		    newargv[i] = interp.getResult();
		} else if (argv[i].type == Thing.GROUP) {
		    /* If it's a GROUP, we pick out the SUBST elements
		     * and eval those before passing the whole thing
		     * off. */
		    StringBuffer result = new StringBuffer("");
		    Vector v = argv[i].getGroup();
		    for (Enumeration e = v.elements();
			 e.hasMoreElements(); ) {
 			Thing t = (Thing)e.nextElement();

			if (t.type == Thing.SUBST) {
			    eval.eval(interp, t);
			    result.append(interp.getResult().toString());
			} else {
			    result.append(t.toString());
			}
		    }
		    newargv[i] = new Thing(result);
		} else {
		    newargv[i] = argv[i];
		}
	    }

/*   	    System.out.println("COMMAND ");
	    for (int i = 0; i < newargv.length; i ++) {
		System.out.println(i + ": " + newargv[i]);
	    }  */

	    try {
		command.cmdCode(interp, newargv);
	    } catch (HeclException e) {
		/* Uh oh, an "issue"! */
		if (newargv[0] != null) {
		    /* Let the exception know where we are. */
		    e.where(newargv[0].toString());
		}
		switch (e.code) {
		    case HeclException.BREAK:
			throw e;
		    case HeclException.CONTINUE:
			throw e;
		    case HeclException.RETURN:
			throw e;
		    case HeclException.ERROR:
			throw e;
		}
	    } catch(Exception e) {
		/* Transform a Java exception into a form more palatable
		 * to Hecl. */
		Vector errors = new Vector();
		StackTraceElement stack[] = e.getStackTrace();
		for (int j = 0; j < stack.length; j ++) {
		    errors.addElement(new Thing(stack[j].toString()));
		}
		Thing newexception = new Thing(errors);
		throw new HeclException(newexception.toString());
	    }

	}

	/**
	 * The <code>toString</code> method turns a Stanza into a
	 * string.
	 *
	 * @return a <code>String</code> value
	 */
	public String toString() {
	    StringBuffer out = new StringBuffer(argv[0].toString());
	    for (int i = 1; i < argv.length; i ++) {
		out.append(" " + argv[i].toString());
	    }
	    return out.toString();
	}
    }
}
