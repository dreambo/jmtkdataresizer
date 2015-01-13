package mtk.resizer.ctrl;

import static mtk.resizer.util.Util.CENT;
import static mtk.resizer.util.Util.ONE;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import mtk.resizer.gui.ResizerPanel;
import mtk.resizer.util.Util;

public class ResizerListener extends MouseAdapter {

	ResizerPanel panel;

	private boolean[] resizes;

	private int totalWidth = 0;
	private boolean sys	   = false;
	private boolean cache  = false;
	private boolean data   = false;

	private boolean changed = false;

	public ResizerListener(ResizerPanel panel) {
		this.panel  =  panel;
	}

	@Override
	public void mouseMoved(MouseEvent e) {

		Point p = e.getPoint();

		if (p.y > ResizerPanel.shift && p.y < (ResizerPanel.shift + ResizerPanel.height)) {

			resizes = new boolean[] {panel.frame.jbSys.getBackground() != Util.DARK, panel.frame.jbCache.getBackground() != Util.DARK, panel.frame.jbData.getBackground() != Util.DARK};
			totalWidth	= panel.getWidth() - (2*ResizerPanel.shift);
			int sysWidth	= Math.round(panel.frame.percents[0] * (totalWidth/((float) CENT)));
			int cacheWidth	= Math.round(panel.frame.percents[1] * (totalWidth/((float) CENT)));
			int dataWidth	= Math.round(panel.frame.percents[2] * (totalWidth/((float) CENT)));
			int x = ResizerPanel.shift;

			// ANDROID resize
			x += sysWidth;
			if (Math.abs(p.x - x) < 3 && resizes[0]) {
				panel.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
				sys = true;
				return;
			}
			// CACHE resize
			x += cacheWidth;
			if (Math.abs(p.x - x) < 3 && resizes[1]) {
				panel.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
				cache = true;
				return;
			}
			// DATA resize
			x += dataWidth;
			if (Math.abs(p.x - x) < 3 && resizes[2]) {
				panel.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
				data = true;
				return;
			}
		}

		sys = cache = data = false;
		panel.setCursor(Cursor.getDefaultCursor());
	}

	@Override
	public void mousePressed(MouseEvent event) {
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		sys = cache = data = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		if (!sys && !cache && !data) {
			return;
		}

		Point p = e.getPoint();
		int sysWidth	= Math.round(panel.frame.percents[0] * (totalWidth/((float) CENT)));
		int cacheWidth	= Math.round(panel.frame.percents[1] * (totalWidth/((float) CENT)));
		int x, percent, percents[] = panel.frame.percents;

		if (sys) {
			x = ResizerPanel.shift;
			percent = Math.round(CENT * (((float) (p.x - x))/totalWidth));
			resize(percents, resizes, percent, 0);

		} else if (cache) {
			x = ResizerPanel.shift + sysWidth;
			percent = Math.round(CENT * (((float) (p.x - x))/totalWidth));
			resize(percents, resizes, percent, 1);

		}  else if (data) {
			x = ResizerPanel.shift + sysWidth + cacheWidth;
			percent = Math.round(CENT * (((float) (p.x - x))/totalWidth));
			resize(percents, resizes, percent, 2);
		}

		changed = (Util.getPercent(percents[0]) != Util.getPercent(panel.frame.iniPercents[0]) ||
				   Util.getPercent(percents[1]) != Util.getPercent(panel.frame.iniPercents[1]) ||
				   Util.getPercent(percents[2]) != Util.getPercent(panel.frame.iniPercents[2]));

		panel.frame.jbReset.setEnabled(changed);
		panel.frame.jbApply.setEnabled(panel.frame.scatterOK && changed);

		panel.repaint();
	}

	private void resize(int[] percents, boolean[] resizes, int percent, int n) {

		if (percents == null || resizes == null || resizes.length != percents.length || n >= percents.length) {
			return;
		}

		for (int i = n + 1; i < percents.length; i++) {
			if (resizes[i]) {
				int totalPercent = percents[n] + percents[i];
				percents[n] = (percent < ONE ? ONE : (percent > totalPercent - ONE ? totalPercent - ONE : percent));
				percent = totalPercent - percents[n];
				percents[i] = (percent < ONE ? ONE : (percent > totalPercent - ONE ? totalPercent - ONE : percent));
				return;
			}
		}

		int totalPercent = percents[n] + CENT - (percents[0] + percents[1] + percents[2]);
		percents[n] = (percent < ONE ? ONE : (percent > totalPercent - ONE ? totalPercent - ONE : percent));
	}
}
