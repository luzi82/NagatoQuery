package com.luzi82.nagatoquery.demo;

import java.util.concurrent.Executors;

import com.luzi82.nagatoquery.NagatoQuery;
import com.luzi82.nagatoquery.NqExec.CommandHandler;
import com.luzi82.nagatoquery.NqSession;
import com.luzi82.nagatoquery.UtilCommand;

public class GolQuery {

	final static String NQ_KEY = "GOL";
	final static String NQ_THREAD_KEY = "GOL_THREAD";

	public static void initNq(NagatoQuery aNagatoQuery, GameOfLife aGameOfLife) {
		aNagatoQuery.mObjTree.put(NQ_KEY, aGameOfLife);
		aNagatoQuery.loadClass(GolQuery.class);
	}

	public static void cmd_setcell(CommandHandler aCommandHandler, int aX, int aY, boolean aValue) {
		GameOfLife gol = (GameOfLife) aCommandHandler.mNagatoQuery.mObjTree.get(NQ_KEY);
		gol.setCell(aX, aY, aValue);
		aCommandHandler.mCommandListener.commandReturn(null);
	}

	public static void cmd_start(CommandHandler aCommandHandler) {
		GameOfLife gol = (GameOfLife) aCommandHandler.mNagatoQuery.mObjTree.get(NQ_KEY);
		gol.start();
		aCommandHandler.mCommandListener.commandReturn(null);
	}

	public static void cmd_stop(CommandHandler aCommandHandler) {
		GameOfLife gol = (GameOfLife) aCommandHandler.mNagatoQuery.mObjTree.get(NQ_KEY);
		gol.stop();
		aCommandHandler.mCommandListener.commandReturn(null);
	}

	public static void cmd_setrect(CommandHandler aCommandHandler, int aX, int aY, int aW, int aH, boolean aValue) {
		GameOfLife gol = (GameOfLife) aCommandHandler.mNagatoQuery.mObjTree.get(NQ_KEY);
		gol.setRect(aX, aY, aW, aH, aValue);
		aCommandHandler.mCommandListener.commandReturn(null);
	}

	public static void main(String[] argv) {
		NagatoQuery nq = new NagatoQuery(Executors.newCachedThreadPool());
		nq.loadClass(UtilCommand.class);
		nq.loadClass(NqSession.class);
		initNq(nq, new GameOfLife(nq.mExecutor));
		NagatoQuery.main(nq);
	}

}
