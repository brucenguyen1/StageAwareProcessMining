package org.processmining.sapm.stagemining.vis;

import java.awt.Color;

public class StageGraphColors {

	private static final Color ABSTRACTBACKGROUNDCOLOR = new Color(120, 140, 248);
	private static final Color ABSTRACTBORDERCOLOR = new Color(20, 20, 20);
	private static final Color ABSTRACTTEXTCOLOR = new Color(10, 10, 10, 240);
	
	private static final Color ADJACENTBACKGROUNDCOLOR = new Color(255, 255, 255);

	private static final Color CLUSTERBACKGROUNDCOLOR = new Color(120, 140, 248);
	private static final Color CLUSTERBORDERCOLOR = new Color(20, 20, 20);
	private static final Color CLUSTERTEXTCOLOR = new Color(10, 10, 10, 240);

	private static final Color EDGECOLOR = new Color(150, 150, 150);
	private static final Color EDGECORRELATEDCOLOR = new Color(20, 20, 20);
	private static final Color EDGEUNCORRELATEDCOLOR = new Color(200, 200, 200);

	private static final Color LABELCOLOR = new Color(120, 120, 120);

	private static final Color PRIMITIVEBACKGROUNDCOLOR = new Color(240, 230, 200);
	private static final Color PRIMITIVEBORDERCOLOR = new Color(20, 20, 20);
	private static final Color PRIMITIVETEXTCOLOR = new Color(0, 0, 0, 230);


	private static Color mix(Color color1, Color color2, float frac2) {
		if (frac2 <= 0f) {
			return color1;
		} else if (frac2 >= 1f) {
			return color2;
		} else {
			float frac1 = 1f - frac2;
			float red = (frac1 * color1.getRed()) + (frac2 * color2.getRed());
			float green = (frac1 * color1.getGreen()) + (frac2 * color2.getGreen());
			float blue = (frac1 * color1.getBlue()) + (frac2 * color2.getBlue());
			float alpha = (frac1 * color1.getAlpha()) + (frac2 * color2.getAlpha());
			return new Color(red / 255f, green / 255f, blue / 255f, alpha / 255f);
		}
	}


	public static Color getAbstractBackgroundColor() {
		return ABSTRACTBACKGROUNDCOLOR;
	}
	
	public static Color getAbstractBorderColor() {
		return ABSTRACTBORDERCOLOR;
	}
	
	public static Color getAbstractTextColor() {
		return ABSTRACTTEXTCOLOR;
	}

	public static Color getAdjacentAbstractBackgroundColor() {
		return mix(ABSTRACTBACKGROUNDCOLOR, ADJACENTBACKGROUNDCOLOR, 0.5f);
	}
	
	public static Color getAdjacentClusterBackgroundColor() {
		return mix(CLUSTERBACKGROUNDCOLOR, ADJACENTBACKGROUNDCOLOR, 0.5f);
	}
	
	public static Color getAdjacentPrimitiveBackgroundColor() {
		return mix(PRIMITIVEBACKGROUNDCOLOR, ADJACENTBACKGROUNDCOLOR, 0.5f);
	}
	
	public static Color getClusterBackgroundColor() {
		return CLUSTERBACKGROUNDCOLOR;
	}
	
	public static Color getClusterBorderColor() {
		return CLUSTERBORDERCOLOR;
	}
	
	public static Color getClusterTextColor() {
		return CLUSTERTEXTCOLOR;
	}

	public static Color getEdgeColor() {
		return EDGECOLOR;
	}
	
	public static Color getEdgeColor(float relativeWeight) {
		return mix(EDGEUNCORRELATEDCOLOR, EDGECORRELATEDCOLOR, relativeWeight);
	}

	public static Color getLabelColor() {
		return LABELCOLOR;
	}
	
	public static Color getPrimitiveBackgroundColor() {
		return PRIMITIVEBACKGROUNDCOLOR;
	}
	
	public static Color getPrimitiveBorderColor() {
		return PRIMITIVEBORDERCOLOR;
	}
	
	public static Color getPrimitiveTextColor() {
		return PRIMITIVETEXTCOLOR;
	}
}
