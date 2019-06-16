package com.comphenix.protocol.wrappers.nbt;

class MemoryElement<TType> implements NbtBase<TType> {
	private String name;
	private TType value;
	private NbtType type;
	
	public MemoryElement(String name, TType value) {
		if (name == null)
			throw new IllegalArgumentException("Name cannot be NULL.");
		if (value == null)
			throw new IllegalArgumentException("Element cannot be NULL.");
		
		this.name = name;
		this.value = value;
		this.type = NbtType.getTypeFromClass(value.getClass());
	}
	
	public MemoryElement(String name, TType value, NbtType type) {
		if (name == null)
			throw new IllegalArgumentException("Name cannot be NULL.");
		if (type == null)
			throw new IllegalArgumentException("Type cannot be NULL.");
		
		this.name = name;
		this.value = value;
		this.type = type;
	}

	@Override
	public boolean accept(NbtVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public NbtType getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public TType getValue() {
		return value;
	}

	@Override
	public void setValue(TType newValue) {
		this.value = newValue;
	}

	@Override
	public NbtBase<TType> deepClone() {
		// This assumes value is an immutable object
		return new MemoryElement<TType>(name, value, type);
	}
}
