package gui.hud.PieChart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Created by Lyncht on 4/28/15.
 */
public class ChartKey extends JComponent
{
  ArrayList<Slice> slices = new ArrayList<>();
  ArrayList<Rectangle> rects = new ArrayList<>();
  Color outline = new Color(0.0f,0.0f,0.0f,1.0f);
  Color textColor = new Color(0.75f,0.75f,0.75f);
  private int rectSide = 0;
  private int width = 0;
  private int individualHeight = 0;
  private int total = 0;
  private int compX = 0;
  private int compY = 0;

  AffineTransform aTrans = new AffineTransform();
  FontRenderContext frc = new FontRenderContext(aTrans,true,true);
  //Convert to ColorsAndFonts when then font is set.
  Font font = new Font("Tahoma", Font.BOLD, 14);
  Font fontSmall = new Font("Tahoma", Font.PLAIN, 6);

  /**
   * ChartKey creates a key for a pie chart based on it's slices
   *
   * @param area of which the chart can lie
   * @param sliceArray, an array of slices (Object Slice)
   */
  public ChartKey(Rectangle area, ArrayList<Slice> sliceArray)
  {
    slices = sliceArray;
    width = (int) area.getWidth();
    total = sliceArray.size();
    individualHeight = (int) (area.getHeight()/total);
    rectSide = (int) (individualHeight*(.50));

    setToolTipText("");
    UIManager.put("ToolTip.background", new Color(0.0f,0.0f,0.0f,1.0f));
    UIManager.put("ToolTip.foreground", new Color(1.0f,1.0f,1.0f,1.0f));
  }

  /**
   * Override paint
   * @param g graphics you wish to have
   */
  @Override
  public void paint(Graphics g)
  {
    drawKey((Graphics2D) g);
  }

  /**
   * drawKey draws all keys for a pie chart
   *
   * @param g2d -Graphics2d called by paint
   */
  private void drawKey(Graphics2D g2d)
  {
    rects.clear();
    for (int i = 0; i < total; i++)
    {
      Slice s = slices.get(i);
      Rectangle sqr = new Rectangle();
      rects.add(sqr);
      int tempY = (individualHeight-rectSide)/2;
      sqr.setBounds(tempY,tempY+compY,rectSide,rectSide);
      g2d.setColor(s.getColor());
      g2d.fill(sqr);
      g2d.setColor(outline);
      g2d.draw(sqr);

      int fontY = (int) (sqr.getY() + font.getSize());
      g2d.setColor(textColor);
      if( rectSide > 10 ) g2d.setFont(font);
      else
      {
        fontY = (int) (sqr.getY() + fontSmall.getSize());
        g2d.setFont(fontSmall);
        System.out.println( "RectSide: " + rectSide + ", " + sqr.getHeight() );
      }
      g2d.drawString(s.getName(), (int) (sqr.getX() + sqr.getWidth() + 10), fontY  );

      compY += individualHeight;
    }
  }

  /**
   * Override getToolTipText to show information about each wedge/slice of the pie chart
   *
   * @param event
   * @return
   */
  @Override
  public String getToolTipText(MouseEvent event)
  {
    Point2D loc = event.getPoint();
    String name = "";
    Double value = 0.0D;
    Double percent = 0.0D;
    for (int i = 0; i < rects.size(); i++) {
      Rectangle r = rects.get(i);
      if( r.contains(loc) )
      {
        Slice s = slices.get(i);
        name = s.getName();
        value = s.getValue();
        percent = s.getPercent();
        break;
      }
    }
    return String.format("<html>%s<br>" + "Value:%.2f<br>" + "Percent:%.2f%%</html>", name, value, percent);
  }
}
