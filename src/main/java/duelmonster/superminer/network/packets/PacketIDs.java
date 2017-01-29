package duelmonster.superminer.network.packets;

public enum PacketIDs {
	GODITEMS(-1), BLOCKINFO(0), Settings_Captivator(1), Settings_Cropinator(2), Settings_Excavator(3), Settings_Illuminator(4), Settings_Lumbinator(5), Settings_Shaftanator(6), Settings_Substitutor(7), Settings_Veinator(8), Substitutor_Update(9), Cropinator_HoePacket(10), Cropinator_CropPacket(11);
	
	private final int value;
	
	PacketIDs(int value) {
		this.value = value;
	}
	
	public int value() {
		return value;
	}
}
