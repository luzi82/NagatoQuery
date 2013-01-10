package com.luzi82.YUKI.N;

public class UtilCommand {

	public static void cmd_echo(NagatoQuery aQuery, NagatoQuery.CommandListener aListener, String aText) {
		aListener.commandReturn(aText);
	}

	public static void cmd_exit(NagatoQuery aQuery, NagatoQuery.CommandListener aListener) {
		aQuery.setVar("%exit", "1");
		aQuery.onExit();
	}

}
