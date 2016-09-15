package duelmonster.superminer.network.packets;

public enum PacketIDs {
	GODITEMS				(-1),
	BLOCKINFO				(0),
	Settings_Captivator		(1),
	Settings_Cropinator		(2),
	Settings_Excavator		(3),
	Settings_Illuminator	(4),
	Settings_Lumbinator		(5),
	Settings_Shaftanator	(6),
	Settings_Substitutor	(7),
	Settings_Veinator		(8),
	Excavator_ShovelPacket	(9),
	Substitutor_Update		(10),
	Cropinator_HoePacket	(11),
	Cropinator_CropPacket	(12);

	private final int value;
	PacketIDs(int value) { this.value = value; }
	public int value() { return value; }
}
