package model;


import gui.GUIRegion;
import gui.WorldPresenter;
import gui.hud.DatePanel;
import gui.hud.PopulationAndHappiness;

import java.util.*;

/**
 * Created by winston on 1/23/15.
 * Phase_01
 * CS 351 spring 2015
 *
 *  The world is everything that is the case.
 *  The world is the totality of facts, not of things.
 *  The facts in logical space are the world.
 *
 *  - L. W.
 */
public class World
{
  private Random random = new Random(44);
  private Collection<Region> world;
  private WorldPresenter worldPresenter;
  private Calendar currentDate;
  private static double EARTH_SURFACE_AREA = 510072000; //km squared
  private static double EQUATORIAL_CIRC = 40075; //Circumference of the earth around the equator
  private static double POLE_CIRC = 20004;  //Distance from pole to pole
  private static int X_CELLS = (int) Math.sqrt((EARTH_SURFACE_AREA/100)*(EQUATORIAL_CIRC/POLE_CIRC));
  private static int Y_CELLS = (int) Math.sqrt((EARTH_SURFACE_AREA/100)/(EQUATORIAL_CIRC/POLE_CIRC)) + 1;
  private WorldArray worldArray;

  /**
   * Class constructor. To build a world one must have a collection of regions.
   * @param world collection of regions that we represent the world
   */
  public World(Collection<Region> world)
  {
    this(world, Calendar.getInstance());
  }

  /**
   * One can also build a world starting at a particular date.
   * @param world
   */
  public World(Collection<Region> world, Calendar cal )
  {
    this.world = world;
    this.currentDate = cal;
    worldArray = new WorldArray(X_CELLS, Y_CELLS, (float) 0.5);
    /*
    for (Region area: world)
    {
      area.setLandCells(worldArray);
    }*/
  }

  public WorldArray getWorldArray()
  {
    return worldArray;
  }

  /**
   * Get the current time of this particular world.
   * @return a calendar object, with the date and time in side.
   */
  public Calendar getCurrentDate()
  {
    return currentDate;
  }

  /**
   * Set the world time to the given calendar date.
   * @param currentDate date the world will be after calling this method.
   */
  public void setCurrentDate(Calendar currentDate)
  {
    this.currentDate = currentDate;
  }

  /**
   * Sets the regions so that the country data can be updated every year
   * @param worldPresenter
   */
  public void setPresenter(WorldPresenter worldPresenter)
  {
    this.worldPresenter = worldPresenter;
  }

  /**
   * Advances the world forward by the given number of days. Every new month
   * the worlds attributes are randomly modulated.
   * @param numOfDays number of days to travel in the future too.
   * @return true - the world was change, false otherwise.
   */
  public boolean setByDays(int numOfDays)
  {
    int previousYear = currentDate.get(Calendar.YEAR);
    currentDate.add(Calendar.DATE, numOfDays);

    DatePanel.updateRatio(currentDate.get(Calendar.DAY_OF_YEAR)/365.0,currentDate.get(Calendar.YEAR));

    if( currentDate.get(Calendar.YEAR) == 2050 )
    {
      //Call to finish game
      main.Game.gameFinished();
      return false;
    }

    boolean isPreviousYear = previousYear != currentDate.get(Calendar.YEAR);
    if (isPreviousYear)
    {
      for( GUIRegion gr: worldPresenter.getAllRegions() ) {
        //Check to ensure it should be updated so that we don't waste time
        if( gr.getOfficialCountry() ) gr.iterateYear();
      }
      for( GUIRegion gr: worldPresenter.getAllRegions() ) {
        //Check to ensure it should be updated so that we don't waste time
        if( gr.getOfficialCountry() ) gr.startIterate();
      }
      while(true)
      {
        if( doneUpdate() )
        {
          //Call to trade
          tradeLeWorldAway();
          break;
        }
      }
      worldArray.updateClimate();
      main.Game.getWorldFeedPanel().update();
    }

    return isPreviousYear;
  }

  private static int totalUpdates = 0;
  public static synchronized boolean doneUpdate()
  {
    if( totalUpdates == 193 )
    {
      totalUpdates = 0;
      return true;
    }
    totalUpdates ++;
    return false;
  }

  /**
   * Sets the initial crops of a country
   */
  public void setAllFirstCrops()
  {
    for (Region area: world)
    {
      area.setFirstCrops();
    }
  }

  private void tradeLeWorldAway()
  {
    //Set level equal to settings panel eventually so that it dictates the difficulty.
    int level = 0;
    ArrayList<GUIRegion> canExport = new ArrayList<>();
    ArrayList<GUIRegion> needImport = new ArrayList<>();
    for( GUIRegion gr: worldPresenter.getAllRegions() )
    {
      if( gr.getOfficialCountry() )
      {
        if( gr.canExport() )
        {
          canExport.add(gr);
        }
        else
        {
          needImport.add(gr);
        }
      }
    }
    switch(level)
    {
      case 2:
        //Hard Algorithm
        break;
      case 1:
        //Medium Algorithm
        break;
      default:
        //Easy Algorithm

    }
  }
}
