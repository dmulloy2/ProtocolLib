package com.comphenix.protocol.wrappers.nbt;

/**
 * A visitor that can enumerate a NBT tree structure.
 * 
 * @author Kristian
 */
public interface NbtVisitor {
	/**
	 * Visit a leaf node, which is a NBT tag with a primitive or String value.
	 * @param node - the visited leaf node.
	 * @return TRUE to continue visiting children at this level, FALSE otherwise.
	 */
	public boolean visit(NbtBase<?> node);
	
	/**
	 * Begin visiting a list node that contains multiple child nodes of the same type.
	 * @param list - the NBT tag to process.
	 * @return TRUE to visit the child nodes of this list, FALSE otherwise.
	 */
	public boolean visitEnter(NbtList<?> list);
	
	/**
	 * Begin visiting a compound node that contains multiple child nodes of different types.
	 * @param compound - the NBT tag to process.
	 * @return TRUE to visit the child nodes of this compound, FALSE otherwise.
	 */
	public boolean visitEnter(NbtCompound compound);
	
	/**
	 * Stop visiting a list node. 
	 * @param list - the list we're done visiting.
	 * @return TRUE for the parent to visit any subsequent sibling nodes, FALSE otherwise.
	 */
	public boolean visitLeave(NbtList<?> list);
	
	/**
	 * Stop visiting a compound node.
	 * @param compound - the compound we're done visting.
	 * @return TRUE for the parent to visit any subsequent sibling nodes, FALSE otherwise
	 */
	public boolean visitLeave(NbtCompound compound);
}
