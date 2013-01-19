package com.luzi82.nagatoquery.demo.desktop;

import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.luzi82.nagatoquery.demo.GameOfLife;
import com.luzi82.nagatoquery.demo.GameOfLife.Listener;

public class GolPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2132539096535714311L;

	final GameOfLife mGameOfLife;
	public static final int CELL_SIZE = 3;

	public GolPanel(GameOfLife aGameOfLife) {
		mGameOfLife = aGameOfLife;
		setSize(GameOfLife.CELL_SIZE * CELL_SIZE, GameOfLife.CELL_SIZE * CELL_SIZE);
		setMinimumSize(getSize());
		setMaximumSize(getSize());
		setPreferredSize(getSize());

		synchronized (mGameOfLife) {
			mGameOfLife.mListenerList.add(new Listener() {
				@Override
				public void onUpdate() {
					repaint();
				}
			});
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		synchronized (mGameOfLife) {
			for (int i = 0; i < GameOfLife.CELL_SIZE; ++i) {
				for (int j = 0; j < GameOfLife.CELL_SIZE; ++j) {
					boolean b = mGameOfLife.getRect(i, j);
					Color c = b ? Color.WHITE : Color.BLACK;
					g.setColor(c);
					g.fillRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
				}
			}
		}
	}

	public static void main(String[] argv) {
		Executor ex = Executors.newCachedThreadPool();

		final GameOfLife gol = new GameOfLife(ex);
		gol.random();

		final GolPanel gp = new GolPanel(gol);
		gp.setVisible(true);
		JFrame jf = new JFrame();
		jf.setContentPane(gp);
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.pack();

		gol.start();
	}
}
