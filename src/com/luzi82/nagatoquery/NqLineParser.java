package com.luzi82.nagatoquery;

import java.text.ParseException;
import java.util.LinkedList;

public class NqLineParser {

	static public class Unit {

	}

	static public class CommandUnit extends Unit {
		public final Unit[] mUnitAry;

		public CommandUnit(Unit[] aUnitAry) {
			mUnitAry = aUnitAry;
		}
	}

	static public class StringUnit extends Unit {
		public final String mString;

		public StringUnit(String aString) {
			mString = aString;
		}
	}

	static public class VarUnit extends Unit {
		public final char mType;
		public final Unit mUnit;

		public VarUnit(char aType, Unit aUnit) {
			mType = aType;
			mUnit = aUnit;
		}
	}

	static public CommandUnit parse(String aLine) throws ParseException {
		Unit[] retCu = new Unit[1];
		int[] retOffset = new int[1];
		parseForward(retCu, retOffset, aLine, 0, ParseType.COMMAND);
		if (retOffset[0] != aLine.length()) {
			throw new ParseException("retOffset[0]!=aLine.length()", retOffset[0]);
		}
		return (CommandUnit) retCu[0];
	}

	enum ParseType {
		COMMAND, VAR
	}

	static private void parseForward(Unit[] aRetCu, int[] aRetOffset, String aLine, int aOffset, ParseType aType) throws ParseException {
		boolean quotation = false;
		boolean slash = false;
		StringBuffer currentString = null;
		LinkedList<Unit> unitList = new LinkedList<NqLineParser.Unit>();
		while (aOffset != aLine.length()) {
			char c = aLine.charAt(aOffset);
			++aOffset;

			if (slash) {
				currentString.append(parseSlash(c));
				slash = false;
			} else if (quotation) {
				if (c == '\\') {
					slash = true;
				} else if (c == '"') {
					unitList.addLast(new StringUnit(currentString.toString()));
					currentString = null;
					quotation = false;
				} else {
					currentString.append(c);
				}
			} else if ((currentString == null) && (c == ' ')) {
				// do nothing
			} else if ((currentString == null) && ((c == '$') || (c == '%'))) {
				Unit[] retCu = new Unit[1];
				int[] retOffset = new int[1];
				parseForward(retCu, retOffset, aLine, aOffset, ParseType.VAR);
				unitList.addLast(new VarUnit(c, retCu[0]));
				aOffset = retOffset[0];
			} else if ((aType == ParseType.VAR) && ((c == ' ') || (c == ')'))) {
				unitList.addLast(new StringUnit(currentString.toString()));
				currentString = null;
				--aOffset;
				break;
			} else if (c == '(') {
				if (currentString != null) {
					throw new ParseException("parentheses", aOffset);
				}
				Unit[] retCu = new Unit[1];
				int[] retOffset = new int[1];
				parseForward(retCu, retOffset, aLine, aOffset, ParseType.COMMAND);
				unitList.addLast(retCu[0]);
				aOffset = retOffset[0];
			} else if (c == ')') {
				break;
			} else if ((currentString != null) && (c == ' ')) {
				unitList.addLast(new StringUnit(currentString.toString()));
				currentString = null;
			} else {
				if (currentString == null)
					currentString = new StringBuffer();
				if (c == '\\') {
					slash = true;
				} else if (c == '"') {
					quotation = true;
				} else {
					currentString.append(c);
				}
			}
		}
		if (currentString != null) {
			unitList.addLast(new StringUnit(currentString.toString()));
			currentString = null;
		}

		if (quotation) {
			throw new ParseException("quotation", aOffset);
		}
		if (slash) {
			throw new ParseException("slash", aOffset);
		}

		if (aType == ParseType.COMMAND) {
			aRetCu[0] = new CommandUnit(unitList.toArray(new Unit[0]));
		} else if (aType == ParseType.VAR) {
			aRetCu[0] = unitList.get(0);
		}
		aRetOffset[0] = aOffset;
	}

	static private char parseSlash(char aChar) {
		return aChar;
	}

}
