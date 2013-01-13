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

import com.luzi82.nagatoquery.NagatoQuery.CommandListener;

public class NqStreamBump {
	public final String mInputPrefix;
	public final BufferedWriter mBufferedWriter;
	public final BufferedReader mBufferedReader;
	public final NagatoQuery mNagatoQuery;

	public NqStreamBump(NagatoQuery aNagatoQuery, InputStream aInputStream, OutputStream aOutputStream, String aInputPrefix) {
		mInputPrefix = aInputPrefix;
		mNagatoQuery = aNagatoQuery;
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
					if (mInputPrefix != null)
						output(mInputPrefix);
					String line = mBufferedReader.readLine();
					mNagatoQuery.execute(line, new CommandListener() {
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
					throw new Error(e);
				}
			}
		};
		mNagatoQuery.mExecutor.execute(r);
	}

	LinkedList<String> mOutputQueue = new LinkedList<String>();
	Runnable mOutputDump = new Runnable() {
		@Override
		public void run() {
			while (true) {
				String out = null;
				synchronized (mOutputQueue) {
					if (mOutputQueue.isEmpty()) {
						mOutputDumpBusy = false;
						return;
					}
					out = mOutputQueue.removeFirst();
				}
				try {
					mBufferedWriter.write(out);
					mBufferedWriter.flush();
				} catch (IOException e) {
					e.printStackTrace();
					synchronized (mOutputQueue) {
						mOutputDumpBusy = false;
						mOutputDump = null;
						return;
					}
				}
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
				mNagatoQuery.mExecutor.execute(mOutputDump);
			}
		}
	}

}
