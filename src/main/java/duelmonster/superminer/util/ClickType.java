package duelmonster.superminer.util;

public enum ClickType {
	PICKUP(0), QUICK_MOVE(1), SWAP(2), CLONE(3), THROW(4), QUICK_CRAFT(5), PICKUP_ALL(6);
	
	private final int index;
	
	private ClickType(int indexIn) {
		this.index = indexIn;
	}
	
	public int getIndex() {
		return this.index;
	}
}