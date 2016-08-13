/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol.reflect.compiler;

import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.asm.Type;

/**
 * Represents a method.
 */
class MethodDescriptor {

    /**
     * The method name.
     */
    private final String name;

    /**
     * The method descriptor.
     */
    private final String desc;

    /**
     * Maps primitive Java type names to their descriptors.
     */
    private static final Map<String, String> DESCRIPTORS;

    static {
        DESCRIPTORS = new HashMap<String, String>();
        DESCRIPTORS.put("void", "V");
        DESCRIPTORS.put("byte", "B");
        DESCRIPTORS.put("char", "C");
        DESCRIPTORS.put("double", "D");
        DESCRIPTORS.put("float", "F");
        DESCRIPTORS.put("int", "I");
        DESCRIPTORS.put("long", "J");
        DESCRIPTORS.put("short", "S");
        DESCRIPTORS.put("boolean", "Z");
    }

    /**
     * Creates a new {@link Method}.
     * 
     * @param name the method's name.
     * @param desc the method's descriptor.
     */
    public MethodDescriptor(final String name, final String desc) {
        this.name = name;
        this.desc = desc;
    }

    /**
     * Creates a new {@link Method}.
     * 
     * @param name the method's name.
     * @param returnType the method's return type.
     * @param argumentTypes the method's argument types.
     */
    public MethodDescriptor(
        final String name,
        final Type returnType,
        final Type[] argumentTypes)
    {
        this(name, Type.getMethodDescriptor(returnType, argumentTypes));
    }

    /**
     * Returns a {@link Method} corresponding to the given Java method
     * declaration.
     * 
     * @param method a Java method declaration, without argument names, of the
     *        form "returnType name (argumentType1, ... argumentTypeN)", where
     *        the types are in plain Java (e.g. "int", "float",
     *        "java.util.List", ...). Classes of the java.lang package can be
     *        specified by their unqualified name; all other classes names must
     *        be fully qualified.
     * @return a {@link Method} corresponding to the given Java method
     *         declaration.
     * @throws IllegalArgumentException if <code>method</code> could not get
     *         parsed.
     */
    public static MethodDescriptor getMethod(final String method)
            throws IllegalArgumentException
    {
        return getMethod(method, false);
    }

    /**
     * Returns a {@link Method} corresponding to the given Java method
     * declaration.
     * 
     * @param method a Java method declaration, without argument names, of the
     *        form "returnType name (argumentType1, ... argumentTypeN)", where
     *        the types are in plain Java (e.g. "int", "float",
     *        "java.util.List", ...). Classes of the java.lang package may be
     *        specified by their unqualified name, depending on the
     *        defaultPackage argument; all other classes names must be fully
     *        qualified.
     * @param defaultPackage true if unqualified class names belong to the
     *        default package, or false if they correspond to java.lang classes.
     *        For instance "Object" means "Object" if this option is true, or
     *        "java.lang.Object" otherwise.
     * @return a {@link Method} corresponding to the given Java method
     *         declaration.
     * @throws IllegalArgumentException if <code>method</code> could not get
     *         parsed.
     */
    public static MethodDescriptor getMethod(
        final String method,
        final boolean defaultPackage) throws IllegalArgumentException
    {
        int space = method.indexOf(' ');
        int start = method.indexOf('(', space) + 1;
        int end = method.indexOf(')', start);
        if (space == -1 || start == -1 || end == -1) {
            throw new IllegalArgumentException();
        }
        String returnType = method.substring(0, space);
        String methodName = method.substring(space + 1, start - 1).trim();
        StringBuffer sb = new StringBuffer();
        sb.append('(');
        int p;
        do {
            String s;
            p = method.indexOf(',', start);
            if (p == -1) {
                s = map(method.substring(start, end).trim(), defaultPackage);
            } else {
                s = map(method.substring(start, p).trim(), defaultPackage);
                start = p + 1;
            }
            sb.append(s);
        } while (p != -1);
        sb.append(')');
        sb.append(map(returnType, defaultPackage));
        return new MethodDescriptor(methodName, sb.toString());
    }

    private static String map(final String type, final boolean defaultPackage) {
        if ("".equals(type)) {
            return type;
        }

        StringBuffer sb = new StringBuffer();
        int index = 0;
        while ((index = type.indexOf("[]", index) + 1) > 0) {
            sb.append('[');
        }

        String t = type.substring(0, type.length() - sb.length() * 2);
        String desc = DESCRIPTORS.get(t);
        if (desc != null) {
            sb.append(desc);
        } else {
            sb.append('L');
            if (t.indexOf('.') < 0) {
                if (!defaultPackage) {
                    sb.append("java/lang/");
                }
                sb.append(t);
            } else {
                sb.append(t.replace('.', '/'));
            }
            sb.append(';');
        }
        return sb.toString();
    }

    /**
     * Returns the name of the method described by this object.
     * 
     * @return the name of the method described by this object.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the descriptor of the method described by this object.
     * 
     * @return the descriptor of the method described by this object.
     */
    public String getDescriptor() {
        return desc;
    }

    /**
     * Returns the return type of the method described by this object.
     * 
     * @return the return type of the method described by this object.
     */
    public Type getReturnType() {
        return Type.getReturnType(desc);
    }

    /**
     * Returns the argument types of the method described by this object.
     * 
     * @return the argument types of the method described by this object.
     */
    public Type[] getArgumentTypes() {
        return Type.getArgumentTypes(desc);
    }

    public String toString() {
        return name + desc;
    }

    public boolean equals(final Object o) {
        if (!(o instanceof MethodDescriptor)) {
            return false;
        }
        MethodDescriptor other = (MethodDescriptor) o;
        return name.equals(other.name) && desc.equals(other.desc);
    }

    public int hashCode() {
        return name.hashCode() ^ desc.hashCode();
    }
}
