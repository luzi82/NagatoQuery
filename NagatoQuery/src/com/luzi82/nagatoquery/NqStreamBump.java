package com.luzi82.nagatoquery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import com.luzi82.nagatoquery.NqSession.CommandListener;

public class NqStreamBump {
	public static final String[] INPUT_PREFIX_KEY_V = { "%SHELL_PREFIX", "$SHELL_PREFIX" };

	public final BufferedWriter mBufferedWriter;
	public final BufferedReader mBufferedReader;
	public final NqSession mNqSession;

	public NqStreamBump(NqSession aNqSession, InputStream aInputStream, OutputStream aOutputStream) {
		mNqSession = aNqSession;
		try {
			mBufferedWriter = new BufferedWriter(new OutputStreamWriter(aOutputStream, "UTF-8"));
			mBufferedReader = new BufferedReader(new InputStreamReader(aInputStream, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}
	}

	public void start() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					for (String key : INPUT_PREFIX_KEY_V) {
						String inputPrefix = null;
						inputPrefix = mNqSession.getVar(key);
						if (inputPrefix != null) {
							output(inputPrefix);
							break;
						}
					}
					String line = mBufferedReader.readLine();
					mNqSession.execute(line, new CommandListener() {
						@Override
						public void commandTrace(String aMessage) {
							output(aMessage + "\n");
						}

						@Override
						public void commandReturn(String aResult) {
							if (aResult != null) {
								output(aResult + "\n");
							}
							start();
						}

						@Override
						public void commandError(String aError) {
							output(aError + "\n");
							start();
						}
					});
				} catch (IOException e) {
					if (mExceptionHandler != null) {
						mExceptionHandler.exception(e);
					}
				}
			}
		};
		mNqSession.mNagatoQuery.mExecutor.execute(r);
	}

	LinkedList<String> mOutputQueue = new LinkedList<String>();
	Runnable mOutputDump = new Runnable() {
		@Override
		public void run() {
			try {
				while (true) {
					String out = null;
					synchronized (mOutputQueue) {
						if (mOutputQueue.isEmpty()) {
							mOutputDumpBusy = false;
							return;
						}
						out = mOutputQueue.removeFirst();
					}
					mBufferedWriter.write(out);
					mBufferedWriter.flush();
				}
			} catch (IOException e) {
				synchronized (mOutputQueue) {
					mOutputQueue.clear();
					mOutputDumpBusy = false;
					mOutputDump = null;
				}
				if (mExceptionHandler != null)
					mExceptionHandler.exception(e);
			}
		}
	};
	boolean mOutputDumpBusy = false;

	public void output(String aMessage) {
		synchronized (mOutputQueue) {
			if (mOutputDump == null)
				return;
			mOutputQueue.addLast(aMessage);
			if (!mOutputDumpBusy) {
				mOutputDumpBusy = true;
				mNqSession.mNagatoQuery.mExecutor.execute(mOutputDump);
			}
		}
	}

	ExceptionHandler mExceptionHandler;

	public void setExceptionHandler(ExceptionHandler aExceptionHandler) {
		this.mExceptionHandler = aExceptionHandler;
	}

	public static void setDefaultPrefix(NagatoQuery aNagatoQuery, String aPrefix) {
		aNagatoQuery.setVar(NqStreamBump.INPUT_PREFIX_KEY_V[NqStreamBump.INPUT_PREFIX_KEY_V.length - 1], aPrefix);
	}

}
