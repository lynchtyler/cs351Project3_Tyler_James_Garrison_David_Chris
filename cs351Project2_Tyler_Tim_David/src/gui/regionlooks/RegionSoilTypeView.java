package gui.regionlooks;


import gui.ColorsAndFonts;
import gui.GUIRegion;
import model.RegionAttributes;

import java.awt.*;

import static model.RegionAttributes.PLANTING_ATTRIBUTES.SOIL_TYPE;

/**
 * Created by David M. on 3/22/2015
 * CS 351 spring 2015
 * <p/>
 * Represents the soil types of the regions.
 */
class RegionSoilTypeView implements RegionView
{
  private static Color[] colors = ColorsAndFonts.SOIL;
  private static double LIMIT = colors.length / RegionAttributes.LIMITS.get(SOIL_TYPE);

  /**
   * Method takes soil types from region and displays it.
   *
   * @param g
   * @param gRegion
   */
  @Override
  public void draw(Graphics g, GUIRegion gRegion)
  {
    if (gRegion == null || gRegion.getRegion().getAttributes() == null)
    {
      System.err.println("(!) GUI REGION or attribute set is null!");
      return;
    }

    double nourish = gRegion.getRegion().getAttributes().getAttribute(SOIL_TYPE);
    Color color;
    if (gRegion.isActive())
    {
      color = Color.CYAN;
    }
    else
    {
      int select = (int) (nourish * LIMIT);
      if(select < colors.length)
        color = colors[select];
      else
        color = colors[colors.length-1];
    }

    g.setColor(color);
    g.fillPolygon(gRegion.getPoly());

    g.setColor(ColorsAndFonts.PASSIVE_REGION_OUTLINE);
    g.drawPolygon(gRegion.getPoly());
  }
}