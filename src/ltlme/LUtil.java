package ltlme;

import java.io.File;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class LUtil {
	static public boolean lineIntersectsRect(double x0, double y0, double x1, double y1,
			double rectx, double recty, double rectw, double recth){
		if(pointInRect(x0, y0, rectx, recty, rectw, recth) 
			&& pointInRect(x1, y1, rectx, recty, rectw, recth))
			return true;
		
		double rx0 = rectx;
		double ry0 = recty;
		double rx1 = rx0 + rectw;
		double ry1 = ry0;
		double rx2 = rx1;
		double ry2 = ry1 + recth;
		double rx3 = rx0;
		double ry3 = ry2;
		return lineIntersectsLine(x0, y0, x1, y1, rx0, ry0, rx1, ry1)
				|| lineIntersectsLine(x0, y0, x1, y1, rx1, ry1, rx2, ry2)
				|| lineIntersectsLine(x0, y0, x1, y1, rx2, ry2, rx3, ry3)
				|| lineIntersectsLine(x0, y0, x1, y1, rx3, ry3, rx0, ry0);
	}
	
	static public boolean pointInRect(double x, double y, double rectx, double recty, double rectw, double recth){
		return rectx < x && x < rectx + rectw && recty < y && y < recty + recth;
	}
	
	static public boolean lineIntersectsLine(double x0, double y0, double x1, double y1,
			double x2, double y2, double x3, double y3){
		double dx0 = x1 - x0;
		double dy0 = y1 - y0;
		x1 = x2;
		y1 = y2;
		double dx1 = x3 - x2;
		double dy1 = y3 - y2;
		double down = dy0 * dx1 - dx0 * dy1;
		if(Math.abs(down) < 0.00000001)return false;
		double dx = x0 - x1;
		double dy = y0 - y1;
		double t0 = (dy1 * dx - dx1 * dy) / down;
		if(t0 < 0 || t0 > 1)return false;
		double t1 = (dy0 * dx - dx0 * dy) / down;
		return t1 >= 0 && t1 <= 1;
	}
	
	public static boolean inWindows = false;
	public static boolean inLinux = true;
	
	static public File getFileDir(File file){
		String dir = null;
		if(inWindows) {
			int index = file.getAbsolutePath().lastIndexOf('\\');
			dir = file.getAbsolutePath().substring(0, index + 1);
		}
		else if(inLinux) {
			int index = file.getAbsolutePath().lastIndexOf('/');
			dir = file.getAbsolutePath().substring(0, index + 1);
		}
		return new File(dir);
	}
	
	static public void enableNodeTranslateMove(Node node) {
		node.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
			node.getProperties().put("last_pos", new Point2D(e.getScreenX(), e.getScreenY()));
		});
		
		node.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
			Object obj = node.getProperties().get("last_pos");
			if(obj == null)return;
			Point2D lastP = (Point2D)obj;
			Point2D nowP = new Point2D(e.getScreenX(), e.getScreenY());
			double transx = node.getTranslateX();
			double transy = node.getTranslateY();
			node.setTranslateX(transx + nowP.getX() - lastP.getX());
			node.setTranslateY(transy + nowP.getY() - lastP.getY());
			node.getProperties().put("last_pos", nowP);
		});
	}
}
