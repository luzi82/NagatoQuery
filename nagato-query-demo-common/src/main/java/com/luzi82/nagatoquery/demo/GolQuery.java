package com.luzi82.nagatoquery.demo;

import java.util.concurrent.Executors;

import com.luzi82.nagatoquery.NagatoQuery;
import com.luzi82.nagatoquery.NqExec.CommandHandler;
import com.luzi82.nagatoquery.NqServer;
import com.luzi82.nagatoquery.NqSession;
import com.luzi82.nagatoquery.NqSession.CommandListener;
import com.luzi82.nagatoquery.NqStreamBump;
import com.luzi82.nagatoquery.UtilCommand;

public class GolQuery {

	final static String NQ_KEY = "GOL";
	final static String NQ_SERVER_KEY = "SERVER";

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

	public static void cmd_randall(CommandHandler aCommandHandler) {
		GameOfLife gol = (GameOfLife) aCommandHandler.mNagatoQuery.mObjTree.get(NQ_KEY);
		gol.random();
		aCommandHandler.mCommandListener.commandReturn(null);
	}

	public static void cmd_setall(CommandHandler aCommandHandler, boolean aValue) {
		GameOfLife gol = (GameOfLife) aCommandHandler.mNagatoQuery.mObjTree.get(NQ_KEY);
		gol.setRect(0, 0, GameOfLife.CELL_SIZE, GameOfLife.CELL_SIZE, aValue);
		aCommandHandler.mCommandListener.commandReturn(null);
	}

	public static void cmd_startserver(CommandHandler aCommandHandler, int aPort) {
		final NagatoQuery nq = aCommandHandler.mNagatoQuery;
		final CommandListener cl = aCommandHandler.mCommandListener;
		if (nq.mObjTree.containsKey(NQ_SERVER_KEY))
			cl.commandError("already running");
		NqServer ns = new NqServer(nq, aPort);
		nq.mObjTree.put(NQ_SERVER_KEY, ns);
		ns.start();
		cl.commandReturn(null);
	}

	public static void cmd_stopserver(CommandHandler aCommandHandler) {
		final NagatoQuery nq = aCommandHandler.mNagatoQuery;
		final CommandListener cl = aCommandHandler.mCommandListener;
		if (!nq.mObjTree.containsKey(NQ_SERVER_KEY))
			cl.commandError("not running");
		NqServer ns = (NqServer) nq.mObjTree.remove(NQ_SERVER_KEY);
		ns.stop();
		cl.commandReturn(null);
	}

	public static void main(String[] argv) {
		NagatoQuery nq = new NagatoQuery(Executors.newCachedThreadPool());
		nq.loadClass(UtilCommand.class);
		nq.loadClass(NqSession.class);
		initNq(nq, new GameOfLife(nq.mExecutor));
		NqStreamBump.setDefaultPrefix(nq, "GOL> ");
		NagatoQuery.main(nq);
	}

}
