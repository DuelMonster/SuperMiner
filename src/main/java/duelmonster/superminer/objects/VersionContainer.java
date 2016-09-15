package duelmonster.superminer.objects;

import java.util.List;

import duelmonster.superminer.SuperMiner_Core;

public class VersionContainer {
	public List<Version> versionList;
	
	public String getModVersion() {
		for (Version version : versionList)
			if (version.getmc_version().equals("Minecraft " + SuperMiner_Core.MCVERSION.replace("[", "").replace("]", "")))
				return version.getModVersion();
		return "Latest Version is Unknown...";
	}

	public static class Version {
		private String mc_version;
		private String modVersion;
		private List<String> changeLog;
		private String updateURL;
		private boolean isDirectLink;
		private String newFileName;

		private Version(String mc_version, String modVersion, List<String> changeLog,
						String updateURL, boolean isDirectLink, String newFileName) {
			this.mc_version = mc_version;
			this.modVersion = modVersion;
			this.changeLog = changeLog;
			this.updateURL = updateURL;
			this.isDirectLink = isDirectLink;
			this.newFileName = newFileName;
		}

		public String getmc_version() { return mc_version; }
		public String getModVersion() { return modVersion; }
		public List<String> getChangeLog() { return changeLog; }
		public String getUpdateURL() { return updateURL; }
		public boolean isDirectLink() { return isDirectLink; }
		public String getNewFileName() { return newFileName; }
	}
}