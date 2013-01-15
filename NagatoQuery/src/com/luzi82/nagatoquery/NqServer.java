package com.luzi82.nagatoquery;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.luzi82.nagatoquery.NqSession.Listener;

public class NqServer implements Runnable {

	final NagatoQuery mNagatoQuery;

	final int mPort;
	ServerSocket mServerSocket;

	boolean mRun = false;

	public NqServer(NagatoQuery aNagatoQuery, int aPort) {
		mPort = aPort;
		mNagatoQuery = aNagatoQuery;
	}

	@Override
	public void run() {
		mRun = true;
		try {
			mServerSocket = new ServerSocket(mPort);
			while (mRun) {
				final Socket socket = mServerSocket.accept();
				NqSession ns = new NqSession(mNagatoQuery);
				ns.setListener(new Listener() {
					@Override
					public void onExit() {
						mRun = false;
						try {
							socket.close();
						} catch (IOException e) {
							// ignore
						}
					}
				});
				NqStreamBump nsb = new NqStreamBump(ns, socket.getInputStream(), socket.getOutputStream(), "GOL> ");
				nsb.setExceptionHandler(mExceptionHandler);
				nsb.start();
			}
		} catch (Exception e) {
			if (mRun) {
				mRun = false;
				if (mExceptionHandler != null) {
					mExceptionHandler.exception(e);
				}
			}
		}
	}

	public void start() {
		mNagatoQuery.mExecutor.execute(this);
	}

	ExceptionHandler mExceptionHandler;

	public void setExceptionHandler(ExceptionHandler aExceptionHandler) {
		mExceptionHandler = aExceptionHandler;
	}

}
