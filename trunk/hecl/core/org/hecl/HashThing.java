/* Copyright 2004-2005 David N. Welton

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

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * The <code>HashThing</code> class represents a hash table type in Hecl.
 * 
 * @author <a href="mailto:davidw@dedasys.com">David N. Welton </a>
 * @version 1.0
 */
public class HashThing implements RealThing {
    private Hashtable val = null;

    /**
     * Creates a new, empty <code>HashThing</code> instance.
     *  
     */
    public HashThing() {
        val = new Hashtable();
    }

    /**
     * Creates a new <code>HashThing</code> instance from a Hashtable.
     * 
     * @param h
     *            a <code>Hashtable</code> value
     */
    public HashThing(Hashtable h) {
        val = h;
    }

    /**
     * Creates a new <code>HashThing</code> instance from a Vector. This may
     * throw an exception, because if the Vector doesn't have an even number of
     * elements, it won't be a valid hash table.
     * 
     * @param v
     *            a <code>Vector</code> value
     * @exception HeclException
     *                if an error occurs
     */
    public HashThing(Vector v) throws HeclException {
        if ((v.size() % 2) != 0) {
            throw new HeclException("list must have even number of elements");
        }
        /*
         * FIXME: I pulled this '3' (initial size of the hash table is list size +
         * 3), out of the air... better suggestions based on experimentation are
         * welcome.
         */
        val = new Hashtable(v.size() + 3);

        for (Enumeration e = v.elements(); e.hasMoreElements();) {
            String key = ((Thing) e.nextElement()).getStringRep();
            Thing value = (Thing) e.nextElement();
            val.put(key, value);
        }
    }

    /**
     * <code>setHashFromAny</code> attempts to create a HashThing from the
     * Thing passed to it.
     * 
     * @param thing
     *            a <code>Thing</code> value
     * @exception HeclException
     *                if an error occurs
     */
    private static void setHashFromAny(Thing thing) throws HeclException {
        RealThing realthing = thing.val;
        Vector list = null;
        HashThing newthing = null;

        if (realthing instanceof HashThing) {
            /* Nothing to be done. */
            return;
        }

        list = ListThing.get(thing);

        newthing = new HashThing(list);
        thing.setVal(newthing);
    }

    /**
     * <code>get</code> attempts to return a Hashtable from a given Thing, in
     * the process transforming that Thing into a HashThing internally.
     * 
     * @param thing
     *            a <code>Thing</code> value
     * @return a <code>Hashtable</code> value
     * @exception HeclException
     *                if an error occurs
     */
    public static Hashtable get(Thing thing) throws HeclException {
        setHashFromAny(thing);
        HashThing gethash = (HashThing) thing.val;
        return gethash.val;
    }

    /**
     * <code>deepcopy</code> copies the hash table and all its elements.
     * 
     * @return a <code>RealThing</code> value
     * @throws HeclException
     */
    public RealThing deepcopy() throws HeclException {
        Hashtable h = new Hashtable();

        for (Enumeration e = val.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            h.put(key, ((Thing) val.get(key)).deepcopy());
        }

        return new HashThing(h);
    }

    /**
     * <code>getStringRep</code> returns a string representation of a
     * HashThing, which is in reality a string representation of a ListThing,
     * only that there are guaranteed to be an even number of elements.
     * 
     * @return a <code>String</code> value
     * @exception HeclException
     *                if an error occurs
     */
    public String getStringRep() throws HeclException {
        Vector v = ListThing.get(new Thing(new HashThing(val)));
        ListThing newthing = new ListThing(v);
        return newthing.getStringRep();
    }

}