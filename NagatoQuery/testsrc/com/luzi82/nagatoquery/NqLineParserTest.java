package com.luzi82.nagatoquery;

import java.text.ParseException;

import junit.framework.Assert;

import org.junit.Test;

import com.luzi82.nagatoquery.NqLineParser.CommandUnit;
import com.luzi82.nagatoquery.NqLineParser.StringUnit;
import com.luzi82.nagatoquery.NqLineParser.VarUnit;

public class NqLineParserTest {

	@Test
	public void testSimple() {
		CommandUnit cu = null;
		StringUnit su = null;

		try {
			cu = NqLineParser.parse("cmd arg");
		} catch (ParseException e) {
			throw new AssertionError(e);
		}
		Assert.assertEquals(2, cu.mUnitAry.length);
		su = (StringUnit) cu.mUnitAry[0];
		Assert.assertEquals("cmd", su.mString);
		su = (StringUnit) cu.mUnitAry[1];
		Assert.assertEquals("arg", su.mString);

		try {
			cu = NqLineParser.parse("cmd a0 a1");
		} catch (ParseException e) {
			throw new AssertionError(e);
		}
		Assert.assertEquals(3, cu.mUnitAry.length);
		su = (StringUnit) cu.mUnitAry[0];
		Assert.assertEquals("cmd", su.mString);
		su = (StringUnit) cu.mUnitAry[1];
		Assert.assertEquals("a0", su.mString);
		su = (StringUnit) cu.mUnitAry[2];
		Assert.assertEquals("a1", su.mString);
	}

	@Test
	public void testVar() {
		CommandUnit cu = null;
		StringUnit su = null;
		VarUnit vu = null;

		try {
			cu = NqLineParser.parse("cmd $arg");
		} catch (ParseException e) {
			throw new AssertionError(e);
		}
		Assert.assertEquals(2, cu.mUnitAry.length);
		su = (StringUnit) cu.mUnitAry[0];
		Assert.assertEquals("cmd", su.mString);
		vu = (VarUnit) cu.mUnitAry[1];
		Assert.assertEquals('$', vu.mType);
		su = (StringUnit) vu.mUnit;
		Assert.assertEquals("arg", su.mString);

		try {
			cu = NqLineParser.parse("$cmd arg");
		} catch (ParseException e) {
			throw new AssertionError(e);
		}
		Assert.assertEquals(2, cu.mUnitAry.length);
		vu = (VarUnit) cu.mUnitAry[0];
		Assert.assertEquals('$', vu.mType);
		su = (StringUnit) vu.mUnit;
		Assert.assertEquals("cmd", su.mString);
		su = (StringUnit) cu.mUnitAry[1];
		Assert.assertEquals("arg", su.mString);
	}

	@Test
	public void testParentheses() {
		CommandUnit cu = null, cu0 = null;
		StringUnit su = null;

		try {
			cu = NqLineParser.parse("cmd ( cmd0 a0 ) a1");
		} catch (ParseException e) {
			throw new AssertionError(e);
		}
		Assert.assertEquals(3, cu.mUnitAry.length);
		su = (StringUnit) cu.mUnitAry[0];
		Assert.assertEquals("cmd", su.mString);
		cu0 = (CommandUnit) cu.mUnitAry[1];
		Assert.assertEquals(2, cu0.mUnitAry.length);
		su = (StringUnit) cu0.mUnitAry[0];
		Assert.assertEquals("cmd0", su.mString);
		su = (StringUnit) cu0.mUnitAry[1];
		Assert.assertEquals("a0", su.mString);
		su = (StringUnit) cu.mUnitAry[2];
		Assert.assertEquals("a1", su.mString);
	}

	@Test
	public void testParentheses0() {
		CommandUnit cu = null, cu0 = null;
		StringUnit su = null;

		try {
			cu = NqLineParser.parse("cmd (cmd0 a0 ) a1");
		} catch (ParseException e) {
			throw new AssertionError(e);
		}
		Assert.assertEquals(3, cu.mUnitAry.length);
		su = (StringUnit) cu.mUnitAry[0];
		Assert.assertEquals("cmd", su.mString);
		cu0 = (CommandUnit) cu.mUnitAry[1];
		Assert.assertEquals(2, cu0.mUnitAry.length);
		su = (StringUnit) cu0.mUnitAry[0];
		Assert.assertEquals("cmd0", su.mString);
		su = (StringUnit) cu0.mUnitAry[1];
		Assert.assertEquals("a0", su.mString);
		su = (StringUnit) cu.mUnitAry[2];
		Assert.assertEquals("a1", su.mString);
	}

	@Test
	public void testParentheses1() {
		CommandUnit cu = null, cu0 = null;
		StringUnit su = null;

		try {
			cu = NqLineParser.parse("cmd ( cmd0 a0) a1");
		} catch (ParseException e) {
			throw new AssertionError(e);
		}
		Assert.assertEquals(3, cu.mUnitAry.length);
		su = (StringUnit) cu.mUnitAry[0];
		Assert.assertEquals("cmd", su.mString);
		cu0 = (CommandUnit) cu.mUnitAry[1];
		Assert.assertEquals(2, cu0.mUnitAry.length);
		su = (StringUnit) cu0.mUnitAry[0];
		Assert.assertEquals("cmd0", su.mString);
		su = (StringUnit) cu0.mUnitAry[1];
		Assert.assertEquals("a0", su.mString);
		su = (StringUnit) cu.mUnitAry[2];
		Assert.assertEquals("a1", su.mString);
	}
	
	@Test
	public void testQuotation(){
		CommandUnit cu = null;
		StringUnit su = null;

		try {
			cu = NqLineParser.parse("\"a\"");
		} catch (ParseException e) {
			throw new AssertionError(e);
		}
		Assert.assertEquals(1, cu.mUnitAry.length);
		su = (StringUnit) cu.mUnitAry[0];
		Assert.assertEquals("a", su.mString);
				
		try {
			cu = NqLineParser.parse("\"a\" b");
		} catch (ParseException e) {
			throw new AssertionError(e);
		}
		Assert.assertEquals(2, cu.mUnitAry.length);
		su = (StringUnit) cu.mUnitAry[0];
		Assert.assertEquals("a", su.mString);
		su = (StringUnit) cu.mUnitAry[1];
		Assert.assertEquals("b", su.mString);
		
		try {
			cu = NqLineParser.parse("a \"b\"");
		} catch (ParseException e) {
			throw new AssertionError(e);
		}
		Assert.assertEquals(2, cu.mUnitAry.length);
		su = (StringUnit) cu.mUnitAry[0];
		Assert.assertEquals("a", su.mString);
		su = (StringUnit) cu.mUnitAry[1];
		Assert.assertEquals("b", su.mString);

		try {
			cu = NqLineParser.parse("\"a\" \"b\"");
		} catch (ParseException e) {
			throw new AssertionError(e);
		}
		Assert.assertEquals(2, cu.mUnitAry.length);
		su = (StringUnit) cu.mUnitAry[0];
		Assert.assertEquals("a", su.mString);
		su = (StringUnit) cu.mUnitAry[1];
		Assert.assertEquals("b", su.mString);
	}
	
	@Test
	public void testQuotation0(){
		CommandUnit cu = null;
		StringUnit su = null;

		try {
			cu = NqLineParser.parse("\" a \"");
		} catch (ParseException e) {
			throw new AssertionError(e);
		}
		Assert.assertEquals(1, cu.mUnitAry.length);
		su = (StringUnit) cu.mUnitAry[0];
		Assert.assertEquals(" a ", su.mString);
	}
}
