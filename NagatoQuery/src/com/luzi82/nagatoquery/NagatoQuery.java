package com.luzi82.nagatoquery;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.luzi82.nagatoquery.NqSession.Listener;

public class NagatoQuery {

	public static final String CMD_PREFIX = "cmd_";

	public final Map<String, String> mVarTree;
	public final Map<String, Object> mCommandTree;
	public final Map<String, Object> mObjTree;

	public final Executor mExecutor;

	public NagatoQuery(Executor aExecutor) {
		mVarTree = new TreeMap<String, String>();
		mCommandTree = new TreeMap<String, Object>();
		mObjTree = new TreeMap<String, Object>();
		mExecutor = aExecutor;
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

	public void loadMethod(String aCmdName, String aMethodId) throws ClassNotFoundException, NoSuchMethodException {
		int lastDotIdx = aMethodId.lastIndexOf('.');
		String className = aMethodId.substring(0, lastDotIdx);
		String methodName = aMethodId.substring(lastDotIdx + 1);
		Class<?> clas = Class.forName(className);
		Method[] clasMethod = clas.getMethods();
		Method method = null;
		for (Method m : clasMethod) {
			if (m.getName().equals(methodName)) {
				method = m;
				break;
			}
		}
		if (method == null) {
			throw new NoSuchMethodException();
		}
		mCommandTree.put(aCmdName, method);
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

	public void setVar(String aKey, Object aObject) {
		String objString = (String) aObject;
		if (aKey.startsWith("$")) {
			mVarTree.put(aKey.substring(1), objString);
		} else {
			throw new IllegalArgumentException();
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
			argTypeV = Arrays.copyOfRange(argTypeV, 1, argTypeV.length);
		} else if (cmdObject instanceof Runnable) {
			argTypeV = new Class<?>[0];
		} else {
			throw new RuntimeException();
		}
		sb.append(argListToString(argTypeV));

		return sb.toString();
	}

	public static void main(String[] argv) {
		NagatoQuery nq = new NagatoQuery(Executors.newCachedThreadPool());
		nq.loadClass(UtilCommand.class);
		nq.loadClass(NqSession.class);
		main(nq);
	}

	public static void main(NagatoQuery aNagatoQuery) {
		NqSession ns = new NqSession(aNagatoQuery);
		ns.setListener(new Listener() {
			@Override
			public void onExit() {
				System.exit(0);
			}
		});
		NqStreamBump nsb = new NqStreamBump(ns, System.in, System.out, "YUKI.N> ");
		nsb.start();
	}

	public static class ConvertException extends Exception {
		private static final long serialVersionUID = -5163271642328951020L;
	}

}
