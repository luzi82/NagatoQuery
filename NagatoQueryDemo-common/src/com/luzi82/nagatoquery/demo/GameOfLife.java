package com.luzi82.nagatoquery.demo;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Executor;

public class GameOfLife {

	public static final int CELL_INDEX_MASK = 0xff;
	public static final int CELL_SIZE = 0x100;

	private final Random mRandom = new Random();
	private final int[][] mCell = new int[CELL_SIZE][CELL_SIZE];

	private final Executor mExecutor;

	public GameOfLife(Executor aExecutor) {
		mExecutor = aExecutor;
	}

	public synchronized void random() {
		for (int i = 0; i < CELL_SIZE; ++i) {
			for (int ii = 0; ii < CELL_SIZE; ++ii) {
				mCell[i][ii] = mRandom.nextBoolean() ? 1 : 0;
			}
		}
		notifyUpdate();
	}

	public synchronized void calc() {
		final int[][] cellTmp = new int[CELL_SIZE][CELL_SIZE];
		for (int i = 0; i < CELL_SIZE; ++i) {
			for (int ii = 0; ii < CELL_SIZE; ++ii) {
				for (int j = -1; j < 2; ++j) {
					for (int jj = -1; jj < 2; ++jj) {
						cellTmp[i][ii] += mCell[(i + j) & CELL_INDEX_MASK][(ii + jj) & CELL_INDEX_MASK];
					}
				}
				cellTmp[i][ii] <<= 1;
				cellTmp[i][ii] -= mCell[i][ii];
			}
		}
		for (int i = 0; i < CELL_SIZE; ++i) {
			for (int ii = 0; ii < CELL_SIZE; ++ii) {
				int c = cellTmp[i][ii];
				mCell[i][ii] = ((c >= 5) && (c < 8)) ? 1 : 0;
			}
		}
		notifyUpdate();
	}

	public interface Listener {
		public void onUpdate();
	}

	public LinkedList<Listener> mListenerList = new LinkedList<GameOfLife.Listener>();

	public synchronized void notifyUpdate() {
		for (Listener l : mListenerList) {
			l.onUpdate();
		}
	}

	public class CalcRunner implements Runnable {
		public boolean mRun = true;

		@Override
		public void run() {
			while (mRun) {
				calc();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private CalcRunner mCalcRunner;

	public synchronized void start() {
		if (mCalcRunner != null)
			return;
		mCalcRunner = new CalcRunner();
		mExecutor.execute(mCalcRunner);
	}

	public synchronized void stop() {
		if (mCalcRunner == null)
			return;
		mCalcRunner.mRun = false;
		mCalcRunner = null;
	}

	public synchronized void setCell(int aX, int aY, boolean aValue) {
		mCell[aX & GameOfLife.CELL_INDEX_MASK][aY & GameOfLife.CELL_INDEX_MASK] = aValue ? 1 : 0;
		notifyUpdate();
	}

	public synchronized void setRect(int aX, int aY, int aW, int aH, boolean aValue) {
		if (aW > GameOfLife.CELL_SIZE)
			aW = GameOfLife.CELL_SIZE;
		if (aH > GameOfLife.CELL_SIZE)
			aH = GameOfLife.CELL_SIZE;
		for (int x = 0; x < aW; ++x) {
			for (int y = 0; y < aH; ++y) {
				mCell[(aX + x) & GameOfLife.CELL_INDEX_MASK][(aY + y) & GameOfLife.CELL_INDEX_MASK] = aValue ? 1 : 0;
			}
		}
		notifyUpdate();
	}

	public synchronized boolean getRect(int aX, int aY) {
		return mCell[aX & GameOfLife.CELL_INDEX_MASK][aY & GameOfLife.CELL_INDEX_MASK] != 0;
	}

}
