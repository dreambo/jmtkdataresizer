package mtk.resizer.gui;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class ResizerPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private int dataPercent;

	public void setDataPercent(int dataPercent) {
		this.dataPercent = dataPercent;
	}

	public ResizerPanel(int dataPercent) {
		this.dataPercent = dataPercent;
	}

	public void paint(java.awt.Graphics g) {
		super.paint(g);

		Graphics2D cg = (Graphics2D) g;

    	int shift		=  10;
		int height		=  30;
		int width		= getWidth() - 20;
		int widthData	= Math.round(dataPercent * (width/100f));
		int widthStor	= width - widthData;

		// data
		cg.setColor(Color.BLUE);
		cg.fill3DRect(shift, shift, widthData, height, true);
		cg.setColor(Color.WHITE);
		cg.drawString(dataPercent + "%", widthData/2, shift + 4 + height/2);
		// storage
		cg.setColor(Color.GREEN);
		cg.fill3DRect(widthData + shift, shift, widthStor, height, true);
		cg.setColor(Color.BLACK);
		cg.drawString((100 - dataPercent) + "%", widthData + widthStor/2, shift + 4 + height/2);
	}
}
