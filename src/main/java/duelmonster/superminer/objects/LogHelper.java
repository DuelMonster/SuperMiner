package duelmonster.superminer.objects;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraftforge.fml.common.FMLLog;

public class LogHelper {
	private static Logger logger = Logger.getLogger("SuperMiner");
	public static void init() { logger.setParent((Logger)FMLLog.getLogger()); }
	public static void log(Level logLevel, String message) { if (System.getProperty("DEBUG") != null) logger.log(logLevel, message); }
}