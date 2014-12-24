package mtk.resizer.ctrl;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import mtk.resizer.gui.ResizerPanel;
import mtk.resizer.util.Util;
import static mtk.resizer.util.Util.CENT;

public class ResizerListener extends MouseAdapter {

	ResizerPanel panel;

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
		boolean resize = false;
		totalWidth	= panel.getWidth() - (2*ResizerPanel.shift);
		int sysWidth	= Math.round(panel.frame.percents[0] * (totalWidth/((float) CENT)));
		int cacheWidth	= Math.round(panel.frame.percents[1] * (totalWidth/((float) CENT)));
		int dataWidth	= Math.round(panel.frame.percents[2] * (totalWidth/((float) CENT)));
		int x = ResizerPanel.shift;

		if (p.y > ResizerPanel.shift && p.y < (ResizerPanel.shift + ResizerPanel.height)) {
			// ANDROID/CACHE resize
			x += sysWidth;
			resize = panel.frame.jbSys.getBackground() != Color.BLACK;
			if (Math.abs(p.x - x) < 2 && resize) {
				panel.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
				sys = true;
				return;
			}
			// CACHE/DATA resize
			x += cacheWidth;
			resize = panel.frame.jbCache.getBackground() != Color.BLACK;
			if (Math.abs(p.x - x) < 2 && resize) {
				panel.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
				cache = true;
				return;
			}
			// DATA/FAT resize
			x += dataWidth;
			resize = panel.frame.jbData.getBackground() != Color.BLACK;
			if (Math.abs(p.x - x) < 2 && resize) {
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
		//int dataWidth	= Math.round(panel.frame.percents[2]  * (totalWidth/((float) CENT)));
		int x, percent, pTotal, ONE = Math.round(CENT/100f);
		boolean resize;

		if (sys) {
			x = ResizerPanel.shift;
			resize = panel.frame.jbCache.getBackground() != Color.BLACK;
			boolean dataResize = panel.frame.jbData.getBackground() != Color.BLACK;

			int i = (resize ? 1 : 2);
			pTotal = (dataResize || resize ? panel.frame.percents[0] + panel.frame.percents[i] : (CENT - (panel.frame.percents[1] + panel.frame.percents[2])));
			percent = Math.round(CENT * (((float) (p.x - x))/totalWidth));
			panel.frame.percents[0] = (percent > 0 ? (percent < pTotal ? percent : pTotal - ONE) : ONE);
			panel.frame.percents[i] = (dataResize || resize ? pTotal - panel.frame.percents[0] : panel.frame.percents[i]);

		} else if (cache) {
			x = ResizerPanel.shift + sysWidth;
			resize = panel.frame.jbData.getBackground() != Color.BLACK;
			pTotal = (resize ? panel.frame.percents[1] + panel.frame.percents[2] : CENT - (panel.frame.percents[0] + panel.frame.percents[2]));
			percent = Math.round(CENT * (((float) (p.x - x))/totalWidth));
			panel.frame.percents[1] = (percent > 0 ? (percent < pTotal ? percent : pTotal - ONE) : ONE);
			panel.frame.percents[2] = (resize ? pTotal - panel.frame.percents[1] : panel.frame.percents[2]);

		}  else if (data) {
			x = ResizerPanel.shift + sysWidth + cacheWidth;
			pTotal = CENT - (panel.frame.percents[0] + panel.frame.percents[1]);
			percent = Math.round(CENT * (((float) (p.x - x))/totalWidth));
			panel.frame.percents[2] = (percent > 0 ? (percent < pTotal ? percent : pTotal - ONE) : ONE);
		}

		changed = (Util.getPercent(panel.frame.percents[0]) != Util.getPercent(panel.frame.iniPercents[0]) ||
				   Util.getPercent(panel.frame.percents[1]) != Util.getPercent(panel.frame.iniPercents[1]) ||
				   Util.getPercent(panel.frame.percents[2]) != Util.getPercent(panel.frame.iniPercents[2]));

		panel.frame.jbReset.setEnabled(changed);
		panel.frame.jbApply.setEnabled(panel.frame.scatterOK && changed);

		//points[pos].setRect(p.x, p.y, points[pos].getWidth(), points[pos].getHeight());
		panel.repaint();
	}
}
