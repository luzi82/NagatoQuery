package com.luzi82.nagatoquery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.luzi82.nagatoquery.NqLineParser.CommandUnit;
import com.luzi82.nagatoquery.NqLineParser.StringUnit;
import com.luzi82.nagatoquery.NqLineParser.Unit;
import com.luzi82.nagatoquery.NqLineParser.VarUnit;

public class NagatoQuery {

	public static final String CMD_PREFIX = "cmd_";

	public final Map<String, String> mVarTree;
	public final Map<String, String> mTmpVarTree;
	public final Map<String, Object> mCommandTree;

	public final Executor mExecutor;

	public NagatoQuery(Executor aExecutor) {
		mVarTree = new TreeMap<String, String>();
		mTmpVarTree = new TreeMap<String, String>();
		mCommandTree = new TreeMap<String, Object>();
		mExecutor = aExecutor;
	}

	public interface CommandListener {
		public void commandTrace(String aMessage);
		
		public void commandReturn(String aResult);

		public void commandError(String aError);
	}

	public abstract class FwErrCommandListener implements CommandListener {
		final CommandListener mListener;
		
		public FwErrCommandListener(CommandListener aListener) {
			mListener = aListener;
		}
		
		public void commandTrace(String aMessage){
			mListener.commandTrace(aMessage);
		}

		public void commandError(String aError) {
			mListener.commandError(aError);
		}
	}

	public void loadClass(Class<?> aClass) {
		Method[] mv = aClass.getMethods();
		for (Method m : mv) {
			String name = m.getName();
			if (name.startsWith(CMD_PREFIX)) {
				name = name.substring(CMD_PREFIX.length());
				mCommandTree.put(name, m);
			}
		}
	}

	public void execute(String aCommand, CommandListener aListener) {
		CommandUnit commandUnit = null;
		try {
			commandUnit = NqLineParser.parse(aCommand);
		} catch (ParseException e) {
			aListener.commandError(e.getMessage());
		}
		execute(commandUnit, aListener);
	}

	class Execution implements Runnable {
		final Unit mUnit;
		final CommandListener mListener;
		String[] mArg = null;
		int mArgOffset = 0;

		public Execution(Unit aUnit, CommandListener aListener) {
			mUnit = aUnit;
			mListener = aListener;
		}

		@Override
		public synchronized void run() {
			if (mUnit instanceof StringUnit) {
				StringUnit u = (StringUnit) mUnit;
				mListener.commandReturn(u.mString);
			} else if (mUnit instanceof VarUnit) {
				VarUnit u = (VarUnit) mUnit;
				if (mArg == null) {
					mArg = new String[1];
					execute(u.mUnit, new FwErrCommandListener(mListener) {
						@Override
						public void commandReturn(String aResult) {
							mArg[0] = aResult;
							start();
						}
					});
				} else {
					char varType = u.mType;
					if (varType == '$') {
						mListener.commandReturn(mVarTree.get(mArg[0]));
					} else if (varType == '%') {
						mListener.commandReturn(mTmpVarTree.get(mArg[0]));
					} else {
						mListener.commandError("varType = " + varType);
					}
				}
			} else if (mUnit instanceof CommandUnit) {
				CommandUnit u = (CommandUnit) mUnit;
				if (mArg == null) {
					mArg = new String[u.mUnitAry.length];
				}
				if (mArgOffset < u.mUnitAry.length) {
					execute(u.mUnitAry[mArgOffset], new FwErrCommandListener(mListener) {
						@Override
						public void commandReturn(String aResult) {
							mArg[mArgOffset] = aResult;
							++mArgOffset;
							start();
						}
					});
				} else {
					execute(mArg, mListener);
				}
			} else {
				mListener.commandError("unknown unit: " + mUnit.getClass().getName());
			}
		}

		public void start() {
			mExecutor.execute(this);
		}

	}

	public void execute(Unit aUnit, CommandListener aListener) {
		Execution ex = new Execution(aUnit, aListener);
		ex.start();
	}

	public void execute(String[] aCommandToken, final CommandListener aListener) {
		String cmdName = aCommandToken[0];
		Object cmdObject = mCommandTree.get(cmdName);
		String[] cmdArg = Arrays.copyOfRange(aCommandToken, 1, aCommandToken.length);
		if (cmdObject == null) {
			aListener.commandError("command not found");
			return;
		}
		if (cmdObject instanceof Method) {
			final Method method = (Method) cmdObject;
			Class<?>[] argTypeV = method.getParameterTypes();
			argTypeV = Arrays.copyOfRange(argTypeV, 2, argTypeV.length);
			if (cmdArg.length != argTypeV.length) {
				aListener.commandError("bad arg: " + methodFormat(cmdName));
				return;
			}
			final Object[] argv = new Object[argTypeV.length + 2];
			argv[0] = this;
			argv[1] = aListener;
			try {
				for (int i = 0; i < argTypeV.length; ++i) {
					argv[i + 2] = convert(cmdArg[i], argTypeV[i]);
				}
			} catch (ConvertException ce) {
				aListener.commandError("bad arg: " + methodFormat(cmdName));
				return;
			}
			mExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						method.invoke(null, argv);
					} catch (Throwable t) {
						aListener.commandError("exception: " + t.getMessage());
					}
				}
			});
		} else if (cmdObject instanceof Runnable) {
			final Runnable cmdRun = (Runnable) cmdObject;
			if (cmdArg.length != 0) {
				aListener.commandError("bad arg: " + methodFormat(cmdName));
				return;
			}
			mExecutor.execute(new Runnable() {
				@Override
				public void run() {
					cmdRun.run();
					aListener.commandReturn(null);
				}
			});
		}
	}

	public static String argListToString(Class<?>[] aArgTypeV) {
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		for (int i = 0; i < aArgTypeV.length; ++i) {
			if (i != 0)
				sb.append(",");
			sb.append(aArgTypeV[i].getSimpleName());
		}
		sb.append(")");
		return sb.toString();
	}

	public static Object convert(String aFrom, Class<?> aTo) throws ConvertException {
		try {
			if (aTo == String.class) {
				return aFrom;
			} else if ((aTo == Character.class) || (aTo == Character.TYPE)) {
				if (aFrom.length() != 1) {
					throw new IllegalArgumentException();
				}
				return aFrom.charAt(0);
			} else if ((aTo == Short.class) || (aTo == Short.TYPE)) {
				return Short.parseShort(aFrom);
			} else if ((aTo == Integer.class) || (aTo == Integer.TYPE)) {
				return Integer.parseInt(aFrom);
			} else if ((aTo == Long.class) || (aTo == Long.TYPE)) {
				return Long.parseLong(aFrom);
			} else if ((aTo == Float.class) || (aTo == Float.TYPE)) {
				return Float.parseFloat(aFrom);
			} else if ((aTo == Double.class) || (aTo == Double.TYPE)) {
				return Double.parseDouble(aFrom);
			} else if ((aTo == Boolean.class) || (aTo == Boolean.TYPE)) {
				return Boolean.parseBoolean(aFrom);
			}
		} catch (NumberFormatException nfe) {
			throw new ConvertException();
		}
		throw new IllegalArgumentException("aTo is " + aTo.toString());
	}

	public String parseInput(String aInput) {
		if (aInput.startsWith("$")) {
			return mVarTree.get(parseInput(aInput.substring(1)));
		} else if (aInput.startsWith("%")) {
			return mTmpVarTree.get(parseInput(aInput.substring(1)));
		} else {
			return aInput;
		}
	}

	public String getVarString(String aKey) {
		String v = parseInput(aKey);
		return v;
	}

	public void setVar(String aKey, Object aObject) {
		String objString = (String) aObject;
		if (aKey.startsWith("$")) {
			mVarTree.put(aKey.substring(1), objString);
		} else if (aKey.startsWith("%")) {
			mTmpVarTree.put(aKey.substring(1), objString);
		} else {
			throw new IllegalArgumentException();
		}
	}

//	public abstract void trace(String aMessage);

	public static class StreamIO extends NagatoQuery implements Runnable {
		public final String mInputPrefix;
		public final InputStream mInputStream;
		public final OutputStream mOutputStream;
		public final BufferedReader mBufferedReader;
		public final BufferedWriter mBufferedWriter;

		public StreamIO(String aInputPrefix, InputStream aInputStream, OutputStream aOutputStream, Executor aExecutor) {
			super(aExecutor);
			mInputPrefix = aInputPrefix;
			mInputStream = aInputStream;
			mOutputStream = aOutputStream;

			try {
				mBufferedReader = new BufferedReader(new InputStreamReader(aInputStream, "UTF-8"));
				mBufferedWriter = new BufferedWriter(new OutputStreamWriter(aOutputStream, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new Error(e);
			}

			loadClass(getClass());
		}

		public void run() {
			String line = readLine();
			execute(line, new CommandListener() {
				@Override
				public void commandReturn(String aResult) {
					if (aResult != null)
						trace(aResult);
					start();
				}
				
				@Override
				public void commandError(String aError) {
					trace(aError);
					start();
				}

				@Override
				public void commandTrace(String aMessage) {
					trace(aMessage);
				}
			});
		}

		public void start() {
			mExecutor.execute(this);
		}

		public String readLine() {
			try {
				if (mInputPrefix != null)
					mBufferedWriter.write(mInputPrefix);
				mBufferedWriter.flush();
				return mBufferedReader.readLine();
			} catch (IOException e) {
				throw new Error(e);
			}
		}

		public void trace(String aMessage) {
			try {
				mBufferedWriter.write(aMessage + "\n");
				mBufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public static void cmd_exit(NagatoQuery aQuery, NagatoQuery.CommandListener aListener, int aExitCode) {
			System.exit(aExitCode);
			aListener.commandReturn(null);
		}

	}

	public String methodFormat(String aCommandName) {
		StringBuffer sb = new StringBuffer();
		sb.append(aCommandName);
		sb.append(" ");

		Class<?>[] argTypeV = null;
		Object cmdObject = mCommandTree.get(aCommandName);
		if (cmdObject == null) {
			throw new IllegalArgumentException();
		} else if (cmdObject instanceof Method) {
			Method method = (Method) cmdObject;
			argTypeV = method.getParameterTypes();
			argTypeV = Arrays.copyOfRange(argTypeV, 2, argTypeV.length);
		} else if (cmdObject instanceof Runnable) {
			argTypeV = new Class<?>[0];
		} else {
			throw new RuntimeException();
		}
		sb.append(argListToString(argTypeV));

		return sb.toString();
	}

	public static void main(String[] argv) {
		StreamIO sc = new StreamIO("YUKI.N> ", System.in, System.out, Executors.newFixedThreadPool(5));
		sc.loadClass(UtilCommand.class);
		sc.start();
	}

	public static class ConvertException extends Exception {
		private static final long serialVersionUID = -5163271642328951020L;
	}

}
