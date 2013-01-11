package com.luzi82.nagatoquery;

public class UtilCommand {

	public static void cmd_echo(NagatoQuery aQuery, NagatoQuery.CommandListener aListener, String aText) {
		aListener.commandReturn(aText);
	}

	public static void cmd_exit(NagatoQuery aQuery, NagatoQuery.CommandListener aListener) {
		aQuery.setVar("%exit", "1");
		aQuery.onExit();
		aListener.commandReturn(null);
	}

	public static void cmd_set(NagatoQuery aQuery, NagatoQuery.CommandListener aListener, String aVarId, String aValue) {
		aQuery.setVar("$" + aVarId, aValue);
		aListener.commandReturn(null);
	}

	public static void cmd_settmp(NagatoQuery aQuery, NagatoQuery.CommandListener aListener, String aVarId, String aValue) {
		aQuery.setVar("%" + aVarId, aValue);
		aListener.commandReturn(null);
	}

}
