package com.luzi82.nagatoquery;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import com.luzi82.nagatoquery.NqExec.CommandHandler;

public class UtilCommand {

	public static void cmd_trace(CommandHandler aCommandHandler, String aText) {
		aCommandHandler.mCommandListener.commandTrace(aText);
		aCommandHandler.mCommandListener.commandReturn(null);
	}

	public static void cmd_set(CommandHandler aCommandHandler, String aVarId, String aValue) {
		aCommandHandler.mNagatoQuery.setVar(aVarId, aValue);
		aCommandHandler.mCommandListener.commandReturn(null);
	}

	@SuppressWarnings("unchecked")
	public static void cmd_lsvar(CommandHandler aCommandHandler) {
		Map.Entry<String, String>[] varList;
		varList = aCommandHandler.mNagatoQuery.mVarTree.entrySet().toArray(new Map.Entry[0]);
		sort(varList);
		for (Map.Entry<String, String> me : varList) {
			aCommandHandler.mCommandListener.commandTrace("$" + me.getKey() + " = " + me.getValue());
		}
		varList = aCommandHandler.mNqSession.mVarTree.entrySet().toArray(new Map.Entry[0]);
		sort(varList);
		for (Map.Entry<String, String> me : varList) {
			aCommandHandler.mCommandListener.commandTrace("%" + me.getKey() + " = " + me.getValue());
		}
		aCommandHandler.mCommandListener.commandReturn(null);
	}

	public static void cmd_help(CommandHandler aCommandHandler) {
		String[] cmdList = aCommandHandler.mNagatoQuery.mCommandTree.keySet().toArray(new String[0]);
		Arrays.sort(cmdList);
		for (String cmd : cmdList) {
			aCommandHandler.mCommandListener.commandTrace(aCommandHandler.mNagatoQuery.methodFormat(cmd));
		}
		aCommandHandler.mCommandListener.commandReturn(null);
	}

	public static void cmd_now(CommandHandler aCommandHandler) {
		final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");
		aCommandHandler.mCommandListener.commandReturn(FORMAT.format(new Date()));
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
