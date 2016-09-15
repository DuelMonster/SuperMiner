package duelmonster.superminer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract interface IClientCode {
	@SideOnly(Side.CLIENT)
	public abstract void exectuteClientCode();
}
