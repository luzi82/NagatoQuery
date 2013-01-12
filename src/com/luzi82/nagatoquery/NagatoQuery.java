package com.luzi82.nagatoquery;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public abstract class NagatoQuery {

	public static final String CMD_PREFIX = "cmd_";

	public final Map<String, String> mVarTree;
	public final Map<String, String> mTmpVarTree;
	public final Map<String, Method> mCommandTree;

	public final Executor mExecutor;

	public NagatoQuery(Executor aExecutor) {
		mVarTree = new TreeMap<String, String>();
		mTmpVarTree = new TreeMap<String, String>();
		mCommandTree = new TreeMap<String, Method>();
		mExecutor = aExecutor;
	}

	public interface CommandListener {
		public void commandReturn(String aResult);
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
		try {
			String[] token = token(aCommand);
			if (token.length <= 0) {
				aListener.commandReturn(null);
				return;
			}
			for (int i = 0; i < token.length; ++i) {
				token[i] = parseInput(token[i]);
			}
			execute(token, aListener);
		} catch (Throwable t) {
			t.printStackTrace();
			aListener.commandReturn(null);
		}
	}

	public void execute(String[] aCommandToken, CommandListener aListener) throws Throwable {
		Method method = mCommandTree.get(aCommandToken[0]);
		Class<?>[] argTypeV = method.getParameterTypes();
		Object[] argv = new Object[aCommandToken.length + 1];
		argv[0] = this;
		argv[1] = aListener;
		for (int i = 2; i < argTypeV.length; ++i) {
			argv[i] = convert(aCommandToken[i - 1], argTypeV[i]);
		}
		method.invoke(null, argv);
	}

	public static Object convert(String aFrom, Class<?> aTo) {
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

	public static String[] token(String aLine) {
		String[] v = aLine.split(Pattern.quote(" "));
		LinkedList<String> vv = new LinkedList<String>();
		for (String s : v) {
			if (s.length() == 0)
				continue;
			vv.addLast(s);
		}
		return vv.toArray(new String[0]);
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

	public abstract void trace(String aMessage);

	public static abstract class AbstractConsole extends NagatoQuery implements Runnable {
		public AbstractConsole(Executor aExecutor) {
			super(aExecutor);
		}

		public void run() {
			if (mTmpVarTree.containsKey("exit"))
				return;
			String line = readLine();
			execute(line, new CommandListener() {
				@Override
				public void commandReturn(String aResult) {
					if (aResult != null)
						trace(aResult);
					start();
				}
			});
		}

		public void start() {
			mExecutor.execute(AbstractConsole.this);
		}

		public abstract String readLine();
	}

	public static class StdConsole extends AbstractConsole {
		public String mInputPrefix;

		public StdConsole(String aInputPrefix, Executor aExecutor) {
			super(aExecutor);
			mInputPrefix = aInputPrefix;
		}

		@Override
		public String readLine() {
			if (mInputPrefix != null)
				System.console().writer().write(mInputPrefix);
			System.console().writer().flush();
			return System.console().readLine();
		}

		@Override
		public void trace(String aMessage) {
			System.console().writer().println(aMessage);
			System.console().writer().flush();
		}

	}

	public static void main(String[] argv) {
		StdConsole sc = new StdConsole("YUKI.N> ", Executors.newFixedThreadPool(5));
		sc.loadClass(UtilCommand.class);
		sc.start();
	}

}
