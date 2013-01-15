package com.luzi82.nagatoquery;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public class NqServer implements Runnable {

	final NagatoQuery mNagatoQuery;
	final int mPort;

	Selector mSelector;
	ServerSocketChannel mServerSocketChannel;

	boolean mRun = false;

	public NqServer(NagatoQuery aNagatoQuery, int aPort) {
		mPort = aPort;
		mNagatoQuery = aNagatoQuery;
	}

	@Override
	public void run() {
		try {
			mSelector = Selector.open();

			mServerSocketChannel = ServerSocketChannel.open();
			mServerSocketChannel.configureBlocking(false);
			mServerSocketChannel.socket().bind(new InetSocketAddress(mPort));

			mServerSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);

			mRun = true;
			while (mRun) {
				mSelector.select(1000);

				if (!mRun)
					break;

				Iterator<SelectionKey> ski = mSelector.selectedKeys().iterator();

				while (ski.hasNext()) {
					SelectionKey sk = (SelectionKey) ski.next();
					ski.remove();
					
					if(sk.channel().getClass()==ServerSocketChannel.class){
						
					}
				}
			}
		} catch (IOException e) {
			if (mExceptionHandler != null)
				mExceptionHandler.exception(e);
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
