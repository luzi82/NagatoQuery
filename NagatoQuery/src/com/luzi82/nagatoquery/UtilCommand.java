package com.luzi82.nagatoquery;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

public class UtilCommand {

	public static void cmd_trace(NagatoQuery aQuery, NagatoQuery.CommandListener aListener, String aText) {
		aListener.commandTrace(aText);
		aListener.commandReturn(null);
	}

	public static void cmd_setvar(NagatoQuery aQuery, NagatoQuery.CommandListener aListener, String aVarId, String aValue) {
		aQuery.setVar("$" + aVarId, aValue);
		aListener.commandReturn(null);
	}

	public static void cmd_settmp(NagatoQuery aQuery, NagatoQuery.CommandListener aListener, String aVarId, String aValue) {
		aQuery.setVar("%" + aVarId, aValue);
		aListener.commandReturn(null);
	}

	@SuppressWarnings("unchecked")
	public static void cmd_lsvar(NagatoQuery aQuery, NagatoQuery.CommandListener aListener) {
		Map.Entry<String, String>[] varList;
		varList = aQuery.mVarTree.entrySet().toArray(new Map.Entry[0]);
		sort(varList);
		for (Map.Entry<String, String> me : varList) {
			aListener.commandTrace("$" + me.getKey() + " = " + me.getValue());
		}
		varList = aQuery.mTmpVarTree.entrySet().toArray(new Map.Entry[0]);
		sort(varList);
		for (Map.Entry<String, String> me : varList) {
			aListener.commandTrace("%" + me.getKey() + " = " + me.getValue());
		}
		aListener.commandReturn(null);
	}

	public static void cmd_help(NagatoQuery aQuery, NagatoQuery.CommandListener aListener) {
		String[] cmdList = aQuery.mCommandTree.keySet().toArray(new String[0]);
		Arrays.sort(cmdList);
		for (String cmd : cmdList) {
			aListener.commandTrace(aQuery.methodFormat(cmd));
		}
		aListener.commandReturn(null);
	}

	public static void cmd_now(NagatoQuery aQuery, NagatoQuery.CommandListener aListener) {
		final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");
		aListener.commandReturn(FORMAT.format(new Date()));
	}

	public static void sort(Map.Entry<String, String>[] aAry) {
		Arrays.sort(aAry, new Comparator<Map.Entry<String, String>>() {
			@Override
			public int compare(Entry<String, String> o1, Entry<String, String> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
	}

}
