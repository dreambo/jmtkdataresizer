package mtk.resizer.main;

import javax.swing.JFrame;

import mtk.resizer.gui.JMTKDataResizer;

public class Main {

	public static void main(String[] args) throws Exception {

    	final JMTKDataResizer display = new JMTKDataResizer();
		display.applyLookAndFeel(-1);
    	display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    	display.init();
    	display.validate();
    	display.pack();
    	display.setVisible(true);
	}
}
