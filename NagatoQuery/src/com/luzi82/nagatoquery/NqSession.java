package com.luzi82.nagatoquery;

import java.text.ParseException;
import java.util.Map;
import java.util.TreeMap;

import com.luzi82.nagatoquery.NqExec.CommandHandler;
import com.luzi82.nagatoquery.NqLineParser.CommandUnit;

public class NqSession {

	public final Map<String, String> mVarTree = new TreeMap<String, String>();
	public final NagatoQuery mNagatoQuery;

	public NqSession(NagatoQuery aNagatoQuery) {
		mNagatoQuery = aNagatoQuery;
	}

	public interface CommandListener {
		public void commandTrace(String aMessage);

		public void commandReturn(String aResult);

		public void commandError(String aError);
	}

	public void execute(String aCommand, CommandListener aListener) {
		CommandUnit commandUnit = null;
		try {
			commandUnit = NqLineParser.parse(aCommand);
		} catch (ParseException e) {
			aListener.commandError(e.getMessage());
		}
		NqExec.execute(mNagatoQuery, this, aListener, commandUnit);
	}

	public void setVar(String aKey, Object aObject) {
		String objString = (String) aObject;
		if (aKey.startsWith("%")) {
			mVarTree.put(aKey.substring(1), objString);
		} else {
			mNagatoQuery.setVar(aKey, aObject);
		}
	}

	public String getVar(String aKey) {
		if (aKey.startsWith("%")) {
			return mVarTree.get(aKey.substring(1));
		} else {
			return mNagatoQuery.getVar(aKey);
		}
	}

	public interface Listener {
		public void onExit();
	}

	Listener mListener;

	public void setListener(Listener aListener) {
		mListener = aListener;
	}

	// public void execute(Unit aUnit, CommandListener aListener) {
	// NqExec ex = new NqExec(this, aUnit, aListener);
	// ex.start();
	// }

	public static void cmd_exit(CommandHandler aCommandHandler) {
		Listener listener = aCommandHandler.mNqSession.mListener;
		if (listener != null) {
			listener.onExit();
		}
	}

}
