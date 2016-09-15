package duelmonster.superminer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract interface IClientCode {
	@SideOnly(Side.CLIENT)
	public abstract void exectuteClientCode();
}
