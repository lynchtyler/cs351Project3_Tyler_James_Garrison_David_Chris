package gui.hud.PieChart;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Class to create a PieChart
 *
 * @author Tyler Lynch <lyncht@unm.edu>
 * @since 4.23.15
 */
public class PieChart extends JComponent
{
  private static double total = 0.0D;
  ArrayList<Slice> slices = new ArrayList<>();

  /**
   * Pie chart creates all of the components for a custom pie chart
   *
   *  Based on a good tutorial from
   *     http://stackoverflow.com/questions/13662984/creating-pie-charts-programmatically
   *
   * @param area of which the chart can lie
   * @param sliceArray, an array of slices (Object Slice)
   */
  public PieChart(Rectangle area, ArrayList<Slice> sliceArray)
  {
    slices = sliceArray;
    double curValue = 0.0D;
    int startAngle;

    // Calculate the length
    for (int i = 0; i < slices.size(); i++) {
      total += slices.get(i).getValue();
    }
    // Calculate arc angles
    for (int i = 0; i < slices.size(); i++) {
      startAngle = (int) (curValue * 360 / total);
      Slice s = slices.get(i);
      s.setAngle((s.getValue() * 360 / total));
      s.setPercent((s.getValue()/total)*100);
      s.setArcStartAngle(startAngle);
      s.setFrame(area);
      curValue += s.getValue();
    }

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
    drawPie((Graphics2D) g);
  }

  /**
   * draw Pie draws all of the slices of a pie chart.
   *
   * @param g2d -Graphics2d called by paint
   */
  private void drawPie(Graphics2D g2d)
  {
    for (int i = 0; i < slices.size(); i++)
    {
      //Get the arc
      Slice s = slices.get(i);

      // Fill the slice/arc
      g2d.setColor(s.getColor() );
      g2d.fill(s.getArc());

      // Draw the outline of the arc -Make more bold first
      g2d.setStroke(new BasicStroke(2));
      g2d.setColor(Color.BLACK);
      g2d.draw( s.getArc() );
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
    for (int i = 0; i < slices.size(); i++) {
      Slice s = slices.get(i);
      if( s.getArc().contains(loc) )
      {
        name = s.getName();
        value = s.getValue();
        percent = s.getPercent();
        break;
      }
    }
    return String.format("<html>%s<br>" + "Value:%.2f<br>" + "Percent:%.2f%%</html>", name, value, percent);
  }
}