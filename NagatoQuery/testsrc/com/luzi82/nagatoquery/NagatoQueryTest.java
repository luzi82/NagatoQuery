package com.luzi82.nagatoquery;

import java.util.LinkedList;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.Test;

public class NagatoQueryTest {

	public static void cmd_helloworld(NagatoQuery aQuery, NagatoQuery.CommandListener aListener) {
		aListener.commandReturn("Hello World");
	}

	public static class TestNq extends NagatoQuery implements NagatoQuery.CommandListener {
		public LinkedList<String> mCommnadReturnRecord = new LinkedList<String>();
		public LinkedList<String> mCommnadTraceRecord = new LinkedList<String>();
		public LinkedList<String> mCommnadErrorRecord = new LinkedList<String>();

		public TestNq() {
			super(Executors.newFixedThreadPool(1));
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
		TestNq nq = new TestNq();
		nq.loadClass(getClass());
		nq.execute("helloworld", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("Hello World", nq.mCommnadReturnRecord.get(0));
	}

	public static void cmd_func_string(NagatoQuery aQuery, NagatoQuery.CommandListener aListener, String aValue) {
		aListener.commandReturn(getCurrentMethodName() + " " + aValue);
	}

	@Test
	public void testArg() {
		TestNq nq = new TestNq();
		nq.loadClass(getClass());
		nq.execute("func_string asdf", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_string asdf", nq.mCommnadReturnRecord.get(0));
	}

	public static void cmd_func_inttype(NagatoQuery aQuery, NagatoQuery.CommandListener aListener, int aValue) {
		aListener.commandReturn(getCurrentMethodName() + " " + aValue);
	}

	@Test
	public void testIntTypeArg() {
		TestNq nq = new TestNq();
		nq.loadClass(getClass());
		nq.execute("func_inttype 123", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_inttype 123", nq.mCommnadReturnRecord.get(0));
	}

	public static void cmd_func_intclass(NagatoQuery aQuery, NagatoQuery.CommandListener aListener, Integer aValue) {
		aListener.commandReturn(getCurrentMethodName() + " " + aValue);
	}

	@Test
	public void testIntClassArg() {
		TestNq nq = new TestNq();
		nq.loadClass(getClass());
		nq.execute("func_intclass 123", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_intclass 123", nq.mCommnadReturnRecord.get(0));
	}

	public static void cmd_func_doubletype(NagatoQuery aQuery, NagatoQuery.CommandListener aListener, double aValue) {
		aListener.commandReturn(getCurrentMethodName() + " " + aValue);
	}

	@Test
	public void testDoubleTypeArg() {
		TestNq nq = new TestNq();
		nq.loadClass(getClass());
		nq.execute("func_doubletype 123", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_doubletype 123.0", nq.mCommnadReturnRecord.get(0));
	}

	public static void cmd_func_doubleclass(NagatoQuery aQuery, NagatoQuery.CommandListener aListener, Double aValue) {
		aListener.commandReturn(getCurrentMethodName() + " " + aValue);
	}

	@Test
	public void testDoubleClassArg() {
		TestNq nq = new TestNq();
		nq.loadClass(getClass());
		nq.execute("func_doubleclass 123", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_doubleclass 123.0", nq.mCommnadReturnRecord.get(0));
	}

	@Test
	public void testVar() {
		TestNq nq = new TestNq();
		nq.loadClass(getClass());
		nq.setVar("$asdf", "qwer");
		nq.execute("func_string $asdf", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_string qwer", nq.mCommnadReturnRecord.get(0));
	}

	@Test
	public void testTmpVar() {
		TestNq nq = new TestNq();
		nq.loadClass(getClass());
		nq.setVar("%asdf", "qwer");
		nq.execute("func_string %asdf", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_string qwer", nq.mCommnadReturnRecord.get(0));
	}

	@Test
	public void testVarRecursive() {
		TestNq nq = new TestNq();
		nq.loadClass(getClass());
		nq.setVar("$v1", "v2");
		nq.setVar("$v2", "v3");
		nq.execute("func_string $$v1", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_string v3", nq.mCommnadReturnRecord.get(0));
	}

	@Test
	public void testTempVarRecursive() {
		TestNq nq = new TestNq();
		nq.loadClass(getClass());
		nq.setVar("%v1", "v2");
		nq.setVar("%v2", "v3");
		nq.execute("func_string %%v1", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("cmd_func_string v3", nq.mCommnadReturnRecord.get(0));
	}

	@Test
	public void testCommandNotFound() {
		TestNq nq = new TestNq();
		nq.execute("not_exist", nq);
		sleep(100);
		Assert.assertEquals(1, nq.mCommnadErrorRecord.size());
		Assert.assertEquals(0, nq.mCommnadReturnRecord.size());
		Assert.assertEquals("command not found", nq.mCommnadErrorRecord.get(0));
	}

	@Test
	public void testArgNumError() {
		TestNq nq = new TestNq();
		nq.loadClass(getClass());

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
		TestNq nq = new TestNq();
		final int[] x = { 0 };
		nq.mCommandTree.put("run", new Runnable() {
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
		TestNq nq = new TestNq();
		nq.mCommandTree.put("run", new Runnable() {
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
		TestNq nq = new TestNq();
		nq.loadClass(getClass());
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
