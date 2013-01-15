package com.luzi82.nagatoquery;

import java.util.LinkedList;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.Test;

import com.luzi82.nagatoquery.NqExec.CommandHandler;

public class NagatoQueryTest {

	public static void cmd_helloworld(CommandHandler aHandler) {
		aHandler.mCommandListener.commandReturn("Hello World");
	}

	public static class TestSession extends NqSession implements NqSession.CommandListener {
		public LinkedList<String> mCommnadReturnRecord = new LinkedList<String>();
		public LinkedList<String> mCommnadTraceRecord = new LinkedList<String>();
		public LinkedList<String> mCommnadErrorRecord = new LinkedList<String>();

		public TestSession() {
			super(new NagatoQuery(Executors.newFixedThreadPool(1)));
		}

		@Override
		public void commandReturn(String aResult) {
			mCommnadReturnRecord.addLast(aResult);
		}

		@Override
		public void commandTrace(String aMessage) {
			mCommnadTraceRecord.addLast(aMessage);
		}

		@Override
		public void commandError(String aError) {
			mCommnadErrorRecord.addLast(aError);
		}

		public void clear() {
			mCommnadReturnRecord.clear();
			mCommnadErrorRecord.clear();
			mCommnadTraceRecord.clear();
		}
	}

	@Test
	public void testHelloWorld() {
		TestSession nq = new TestSession();
		nq.mNagatoQuery.loadClass(getClass());
		nq.execute("helloworld", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("Hello World", nq.mCommnadReturnRecord.get(0));
	}

	public static void cmd_func_string(CommandHandler aHandler, String aValue) {
		aHandler.mCommandListener.commandReturn(getCurrentMethodName() + " " + aValue);
	}

	@Test
	public void testArg() {
		TestSession nq = new TestSession();
		nq.mNagatoQuery.loadClass(getClass());
		nq.execute("func_string asdf", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_string asdf", nq.mCommnadReturnRecord.get(0));
	}

	public static void cmd_func_inttype(CommandHandler aHandler, int aValue) {
		aHandler.mCommandListener.commandReturn(getCurrentMethodName() + " " + aValue);
	}

	@Test
	public void testIntTypeArg() {
		TestSession nq = new TestSession();
		nq.mNagatoQuery.loadClass(getClass());
		nq.execute("func_inttype 123", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_inttype 123", nq.mCommnadReturnRecord.get(0));
	}

	public static void cmd_func_intclass(CommandHandler aHandler, Integer aValue) {
		aHandler.mCommandListener.commandReturn(getCurrentMethodName() + " " + aValue);
	}

	@Test
	public void testIntClassArg() {
		TestSession nq = new TestSession();
		nq.mNagatoQuery.loadClass(getClass());
		nq.execute("func_intclass 123", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_intclass 123", nq.mCommnadReturnRecord.get(0));
	}

	public static void cmd_func_doubletype(CommandHandler aHandler, double aValue) {
		aHandler.mCommandListener.commandReturn(getCurrentMethodName() + " " + aValue);
	}

	@Test
	public void testDoubleTypeArg() {
		TestSession nq = new TestSession();
		nq.mNagatoQuery.loadClass(getClass());
		nq.execute("func_doubletype 123", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_doubletype 123.0", nq.mCommnadReturnRecord.get(0));
	}

	public static void cmd_func_doubleclass(CommandHandler aHandler, Double aValue) {
		aHandler.mCommandListener.commandReturn(getCurrentMethodName() + " " + aValue);
	}

	@Test
	public void testDoubleClassArg() {
		TestSession nq = new TestSession();
		nq.mNagatoQuery.loadClass(getClass());
		nq.execute("func_doubleclass 123", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_doubleclass 123.0", nq.mCommnadReturnRecord.get(0));
	}

	@Test
	public void testVar() {
		TestSession nq = new TestSession();
		nq.mNagatoQuery.loadClass(getClass());
		nq.setVar("$asdf", "qwer");
		nq.execute("func_string $asdf", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_string qwer", nq.mCommnadReturnRecord.get(0));
	}

	@Test
	public void testTmpVar() {
		TestSession nq = new TestSession();
		nq.mNagatoQuery.loadClass(getClass());
		nq.setVar("%asdf", "qwer");
		nq.execute("func_string %asdf", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_string qwer", nq.mCommnadReturnRecord.get(0));
	}

	@Test
	public void testVarRecursive() {
		TestSession nq = new TestSession();
		nq.mNagatoQuery.loadClass(getClass());
		nq.setVar("$v1", "v2");
		nq.setVar("$v2", "v3");
		nq.execute("func_string $$v1", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_string v3", nq.mCommnadReturnRecord.get(0));
	}

	@Test
	public void testTempVarRecursive() {
		TestSession nq = new TestSession();
		nq.mNagatoQuery.loadClass(getClass());
		nq.setVar("%v1", "v2");
		nq.setVar("%v2", "v3");
		nq.execute("func_string %%v1", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_string v3", nq.mCommnadReturnRecord.get(0));
	}

	@Test
	public void testCommandNotFound() {
		TestSession nq = new TestSession();
		nq.execute("not_exist", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadErrorRecord.size());
		Assert.assertEquals(0, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("command not found", nq.mCommnadErrorRecord.get(0));
	}

	@Test
	public void testArgNumError() {
		TestSession nq = new TestSession();
		nq.mNagatoQuery.loadClass(getClass());

		nq.execute("func_string", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadErrorRecord.size());
		Assert.assertEquals(0, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("bad arg: func_string (String)", nq.mCommnadErrorRecord.get(0));

		nq.clear();

		nq.execute("func_string a b", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadErrorRecord.size());
		Assert.assertEquals(0, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("bad arg: func_string (String)", nq.mCommnadErrorRecord.get(0));
	}

	@Test
	public void testRunnable() {
		TestSession nq = new TestSession();
		final int[] x = { 0 };
		nq.mNagatoQuery.mCommandTree.put("run", new Runnable() {
			@Override
			public void run() {
				x[0] = 1;
			}
		});
		nq.execute("run", nq);
		sleep(100);
		Assert.assertEquals(0, nq.mCommnadErrorRecord.size());
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals(null, nq.mCommnadReturnRecord.get(0));
		Assert.assertEquals(1, x[0]);
	}

	@Test
	public void testRunnableArg() {
		TestSession nq = new TestSession();
		nq.mNagatoQuery.mCommandTree.put("run", new Runnable() {
			@Override
			public void run() {
			}
		});
		nq.execute("run x", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadErrorRecord.size());
		Assert.assertEquals(0, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("bad arg: run ()", nq.mCommnadErrorRecord.get(0));
	}

	@Test
	public void testBadArg() {
		TestSession nq = new TestSession();
		nq.mNagatoQuery.loadClass(getClass());
		nq.execute("func_inttype x", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadErrorRecord.size());
		Assert.assertEquals(0, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("bad arg: func_inttype (int)", nq.mCommnadErrorRecord.get(0));
	}

	public static void sleep(long aMs) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String getCurrentMethodName() {
		StackTraceElement[] steV = Thread.currentThread().getStackTrace();
		for (int i = 0; i < steV.length; ++i) {
			StackTraceElement ste = steV[i];
			if (ste.getMethodName() == "getCurrentMethodName") {
				return steV[i + 1].getMethodName();
			}
		}
		throw new Error();
	}

}
