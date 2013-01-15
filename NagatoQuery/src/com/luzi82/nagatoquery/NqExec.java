package com.luzi82.nagatoquery;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.luzi82.nagatoquery.NagatoQuery.ConvertException;
import com.luzi82.nagatoquery.NqLineParser.CommandUnit;
import com.luzi82.nagatoquery.NqLineParser.StringUnit;
import com.luzi82.nagatoquery.NqLineParser.Unit;
import com.luzi82.nagatoquery.NqLineParser.VarUnit;
import com.luzi82.nagatoquery.NqSession.CommandListener;

public class NqExec implements Runnable {
	/**
		 * 
		 */
	// private final NagatoQuery nagatoQuery;
	final Unit mUnit;
	final CommandHandler mCommandHandler;
	final NagatoQuery mNagatoQuery;
	final NqSession mNqSession;
	final CommandListener mCommandListener;
	String[] mArg = null;
	int mArgOffset = 0;

	public NqExec(Unit aUnit, CommandHandler aCommandHandler) {
		mUnit = aUnit;
		mCommandHandler = aCommandHandler;
		mNagatoQuery = mCommandHandler.mNagatoQuery;
		mNqSession = mCommandHandler.mNqSession;
		mCommandListener = mCommandHandler.mCommandListener;
	}

	@Override
	public synchronized void run() {
		if (mUnit instanceof StringUnit) {
			StringUnit u = (StringUnit) mUnit;
			mCommandListener.commandReturn(u.mString);
		} else if (mUnit instanceof VarUnit) {
			VarUnit u = (VarUnit) mUnit;
			if (mArg == null) {
				mArg = new String[1];
				execute(new FwErrCommandHandler(mCommandHandler) {
					@Override
					public void commandReturn(String aResult) {
						mArg[0] = aResult;
						start();
					}
				}, u.mUnit);
			} else {
				char varType = u.mType;
				if (varType == '$') {
					mCommandListener.commandReturn(mNagatoQuery.mVarTree.get(mArg[0]));
				} else if (varType == '%') {
					mCommandListener.commandReturn(mNqSession.mVarTree.get(mArg[0]));
				} else {
					mCommandListener.commandError("varType = " + varType);
				}
			}
		} else if (mUnit instanceof CommandUnit) {
			CommandUnit u = (CommandUnit) mUnit;
			if (mArg == null) {
				mArg = new String[u.mUnitAry.length];
			}
			if (mArgOffset < u.mUnitAry.length) {
				execute(new FwErrCommandHandler(mCommandHandler) {
					@Override
					public void commandReturn(String aResult) {
						mArg[mArgOffset] = aResult;
						++mArgOffset;
						start();
					}
				}, u.mUnitAry[mArgOffset]);
			} else {
				execute(mArg);
			}
		} else {
			mCommandListener.commandError("unknown unit: " + mUnit.getClass().getName());
		}
	}

	public void execute(String[] aCommandToken) {
		if (aCommandToken.length == 0) {
			mCommandListener.commandReturn(null);
			return;
		}
		String cmdName = aCommandToken[0];
		Object cmdObject = mNagatoQuery.mCommandTree.get(cmdName);
		String[] cmdArg = Arrays.copyOfRange(aCommandToken, 1, aCommandToken.length);
		if (cmdObject == null) {
			mCommandListener.commandError("command not found");
			return;
		}
		if (cmdObject instanceof Method) {
			final Method method = (Method) cmdObject;
			Class<?>[] argTypeV = method.getParameterTypes();
			argTypeV = Arrays.copyOfRange(argTypeV, 1, argTypeV.length);
			if (cmdArg.length != argTypeV.length) {
				mCommandListener.commandError("bad arg: " + mNagatoQuery.methodFormat(cmdName));
				return;
			}
			final Object[] argv = new Object[argTypeV.length + 1];
			argv[0] = mCommandHandler;
			try {
				for (int i = 0; i < argTypeV.length; ++i) {
					argv[i + 1] = convert(cmdArg[i], argTypeV[i]);
				}
			} catch (ConvertException ce) {
				mCommandListener.commandError("bad arg: " + mNagatoQuery.methodFormat(cmdName));
				return;
			}
			mNagatoQuery.mExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						method.invoke(null, argv);
					} catch (Throwable t) {
						mCommandListener.commandError("exception: " + t.getMessage());
					}
				}
			});
		} else if (cmdObject instanceof Runnable) {
			final Runnable cmdRun = (Runnable) cmdObject;
			if (cmdArg.length != 0) {
				mCommandListener.commandError("bad arg: " + mNagatoQuery.methodFormat(cmdName));
				return;
			}
			mNagatoQuery.mExecutor.execute(new Runnable() {
				@Override
				public void run() {
					cmdRun.run();
					mCommandListener.commandReturn(null);
				}
			});
		}
	}

	public void start() {
		mNagatoQuery.mExecutor.execute(this);
	}

	static public abstract class FwErrCommandListener implements CommandListener {
		final CommandListener mListener;

		public FwErrCommandListener(CommandListener aListener) {
			mListener = aListener;
		}

		public void commandTrace(String aMessage) {
			mListener.commandTrace(aMessage);
		}

		public void commandError(String aError) {
			mListener.commandError(aError);
		}
	}

	public static class CommandHandler {
		public NagatoQuery mNagatoQuery;
		public NqSession mNqSession;
		public CommandListener mCommandListener;
	}

	public static abstract class FwErrCommandHandler extends CommandHandler {
		public FwErrCommandHandler(CommandHandler aCommandHandler) {
			mNagatoQuery = aCommandHandler.mNagatoQuery;
			mNqSession = aCommandHandler.mNqSession;
			mCommandListener = new FwErrCommandListener(mCommandListener) {
				@Override
				public void commandReturn(String aResult) {
					FwErrCommandHandler.this.commandReturn(aResult);
				}
			};
		}

		public abstract void commandReturn(String aResult);
	}

	public static void execute(NagatoQuery aNagatoQuery, NqSession aNqSession, CommandListener aCommandListener, Unit aUnit) {
		CommandHandler commandHandler = new CommandHandler();
		commandHandler.mNagatoQuery = aNagatoQuery;
		commandHandler.mNqSession = aNqSession;
		commandHandler.mCommandListener = aCommandListener;
		execute(commandHandler, aUnit);

	}

	public static void execute(CommandHandler aCommandHandler, Unit aUnit) {
		NqExec exec = new NqExec(aUnit, aCommandHandler);
		exec.start();
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

}