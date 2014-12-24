package mtk.resizer.gui;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import mtk.resizer.ctrl.ResizerListener;
import mtk.resizer.util.Util;
import static mtk.resizer.util.Util.CENT;

public class ResizerPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public final static int shift		=  10;
	public final static int height		=  40;

	public JMTKResizer frame;

	ResizerListener ada;

	public ResizerPanel(JMTKResizer frame) {

		this.frame = frame;

		ResizerListener ada = new ResizerListener(this);

		addMouseListener(ada);
	    addMouseMotionListener(ada);
	}

	public void paint(java.awt.Graphics g) {
		super.paint(g);

		Graphics2D g2 = (Graphics2D) g;

		int totalWidth	= getWidth() - (2*shift);
		int sysWidth	= Math.round(frame.percents[0] * (totalWidth/((float) CENT)));
		int cacheWidth	= Math.round(frame.percents[1] * (totalWidth/((float) CENT)));
		int dataWidth	= Math.round(frame.percents[2] * (totalWidth/((float) CENT)));
		int fatWidth	= totalWidth - (sysWidth + cacheWidth + dataWidth);
		int x = 0;

		// ANDROID partition
		x += 0;
		g2.setColor(Color.CYAN);
		g2.fill3DRect(x + shift, shift, sysWidth, height, true);
		g2.setColor(Color.BLACK);
		g2.drawRect(x + shift, shift, sysWidth, height);
		g2.drawString(Util.getPercent(frame.percents[0]) + "%", x + sysWidth/2, shift + 4 + height/2);

		// CACHE partition
		x += sysWidth;
		g2.setColor(Color.YELLOW);
		g2.fill3DRect(x + shift, shift, cacheWidth, height, true);
		g2.setColor(Color.BLACK);
		g2.drawRect(x + shift, shift, cacheWidth, height);
		g2.drawString(Util.getPercent(frame.percents[1]) + "%", x + cacheWidth/2, shift + 4 + height/2);

		// USRDATA partition
		x += cacheWidth;
		g2.setColor(Color.BLUE);
		g2.fill3DRect(x + shift, shift, dataWidth, height, true);
		g2.setColor(Color.BLACK);
		g2.drawRect(x + shift, shift, dataWidth, height);
		g2.setColor(Color.WHITE);
		g2.drawString(Util.getPercent(frame.percents[2]) + "%", x + dataWidth/2, shift + 4 + height/2);

		// FAT partition
		x += dataWidth;
		g2.setColor(Color.GREEN);
		g2.fill3DRect(x + shift, shift, fatWidth, height, true);
		g2.setColor(Color.BLACK);
		g2.drawRect(x + shift, shift, fatWidth, height);
		g2.drawString(100 - (Util.getPercent(frame.percents[0]) + Util.getPercent(frame.percents[1]) + Util.getPercent(frame.percents[2])) + "%", x + fatWidth/2, shift + 4 + height/2);

		frame.refreshSize();
	}
}
