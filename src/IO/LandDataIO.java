package IO;


import model.LandTile;
import model.Region;
import model.TileManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static model.LandTile.FIELD.*;
import static model.LandTile.FIELD.ELEVATION;


/**
 @author david
 created: 2015-03-22
 description:
 <p/>
 IO class dedicated to generating and parsing land data.  Nested RawDataParser class is used
 for
 generating a new set o */
public class LandDataIO
{

  public static String DEFAULT_FILE = "resources/data/land-data.bil";
  private static String DEFAULT_BACKUP = "resources/data/landtile-rawdata/datatiles.bak";
  private static boolean DEBUG = true;


  public static void main(String[] args)
  {
    List<Region> regions = new ArrayList(new AreaXMLLoader().getRegions());
    testVisual(parseFile(DEFAULT_FILE, regions), new Dimension(1500, 500));
  }


  /* test a loaded tilemanager's contents visually */
  static void testVisual(final TileManager mgr, final Dimension winSize)
  {
    JFrame win = new JFrame();
    win.setSize(winSize);
    JPanel panel = new JPanel()
    {
      @Override
      public void paintComponent(Graphics g)
      {
        for (LandTile tile : mgr.allTiles()) paintTile(tile, (Graphics2D) g);
      }


      /* define how to paint the LandTile */
      private void paintTile(LandTile tile, Graphics2D g)
      {
        Rectangle2D rect = tileToRect(tile);

        /* arbitrarily use countryname hash to make a color */
        g.setColor(new Color(tile.getIntData(COUNTRYID), true));
        g.fill(rect);
      }


      /* Rectangle defined in terms of winSize...  Fills the window */
      private Rectangle2D tileToRect(LandTile tile)
      {
        double height = winSize.getHeight() / 180;
        double width = winSize.getWidth() / 360;
        double x = width * (tile.getLon() + 180);
        double y = -height * (tile.getLat() - 90);
        return new Rectangle2D.Double(x, y, width, height);
      }
    };

    /* set up the gui */
    panel.setSize(winSize);
    panel.setPreferredSize(winSize);
    win.setContentPane(panel);
    win.pack();
    win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    win.setVisible(true);
    win.repaint();
  }


  /**
   Load a TileManager from a file.  If the file was not written in the correct format, it will not
   load tiles correctly.  New files can be generated from the raw data using RawDataParser nested
   class.  The Regions given will be used to associate the tiles with each region, based on the
   countryID associated with each tile.  IDs are generated from the country name's hashcode,
   currently.  If names change, the raw data will need to be parsed again

   @param filename
   File to load
   @param countries
   Regions to place tiles in

   @return Tilemanager holding all the tiles loaded
   */
  public static TileManager parseFile(String filename, Collection<Region> countries)
  {
    TileManager mgr = parseFile(filename);
    Map<Integer, Region> map = new HashMap<>();
    for (Region r : countries) map.put(r.getName().hashCode(), r);
    for (LandTile t : mgr.dataTiles())
    {
      int hash = t.getIntData(COUNTRYID);
      Region region = map.get(hash);
      region.addLandTile(t);
    }
    return mgr;
  }


  /**
   Load a TileManager from a file.  If the file was not written in the correct format, it will not
   load tiles correctly.  New files can be generated from the raw data using RawDataParser nested
   class

   @param filename
   File to load

   @return Tilemanager holding all the tiles loaded
   */
  static TileManager parseFile(String filename)
  {
    TileManager mgr = new TileManager();
    try (FileInputStream in = new FileInputStream(filename))
    {
      long start = System.currentTimeMillis();
      long count = 0;
      byte bytes[] = new byte[LandTile.FIELD.SIZE_IN_BYTES];
      ByteBuffer buf = ByteBuffer.allocate(LandTile.FIELD.SIZE_IN_BYTES);
      LandTile tile;
      while (in.read(bytes) != -1)
      {
        if (++count % 10000 == 0 && DEBUG)
        {
          System.out.printf("read %d tiles in %fs%n",
                            count, (System.currentTimeMillis() - start) / 1000d);
        }
        buf.clear();
        buf.put(bytes);
        tile = new LandTile(buf);
        mgr.putTile(tile);

      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    if(DEBUG)
    {
      for(LandTile.FIELD f : LandTile.FIELD.FLOAT_FIELDS)
      {
        System.out.println(f);
        System.out.println(LandTile.getMax(f));
        System.out.println(LandTile.getMin(f));
      }
    }
    return mgr;
  }


  /* test method reads a file and checks averages of values across the set */
  private static void validateFile(String file)
  {
    TileManager mgr = parseFile(file);
    Map<RawDataConverter.DATA_FIELD, Float> avgs = new HashMap<>();
    Map<RawDataConverter.DATA_FIELD, Integer> counts = new HashMap<>();

    int nulls = 0;
    int no_data = 0;
    for (LandTile t : mgr.dataTiles())
    {
      if (t == null)
      {
        nulls++;
      }
      else if (t == LandTile.NO_DATA)
      {
        no_data++;
      }
      else
      {
        for (RawDataConverter.DATA_FIELD field : RawDataConverter.DATA_FIELD.values())
        {
          if (!avgs.containsKey(field))
          {
            avgs.put(field, t.getData(field.landTileField));
            counts.put(field, 1);
          }
          else
          {
            float avg = avgs.get(field);
            int count = counts.get(field);
            avg = (avg * count + t.getData(field.landTileField)) / (++count);
            avgs.put(field, avg);
            counts.put(field, count);
          }
        }
      }
    }
    System.out.println("nulls : " + nulls);
    System.out.println("no_data : " + no_data);
    for (RawDataConverter.DATA_FIELD f : RawDataConverter.DATA_FIELD.values())
    {
      System.out.printf("%s average : %f%n", f.toString(), avgs.get(f));
    }
  }


  /**
   Writes ALL tiles in the specified TileManager to file.  If a subset is required, use the
   overloaded version that accepts a Collection of LandTiles

   @param mgr
   TileManger whose tiles are to be written
   @param filename
   Relative path to write to
   */
  public static void writeToFile(TileManager mgr, String filename)
  {
    writeToFile(mgr.allTiles(), filename);
  }


  public static void writeToFile(Collection<LandTile> tiles, String filename)
  {
    try (FileOutputStream out = new FileOutputStream(filename))
    {
      for (LandTile t : tiles)
      {
        out.write(t.toByteBuffer().array());
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }


  private static class RawDataConverter
  {

    /* constants describe the shape of the binary files from which raw data is read */
    private static final double DATA_ARC_STEP = 0.041666666;
    private static final int DATA_ROWS = 3600;
    private static final int DATA_COLS = 8640;

    /* default root of raw data.  Note that this directory is listed in the .gitignore file, because
      these are LARGE files.  I (David) have them on my computer, and
     */
    private static final String DEFAULT_ROOT = "resources/data/landtile-rawdata/";
    private static final int NO_DATA_DEFAULT_CURR = -9999;
    private static final int NO_DATA_DEFAULT_PROJ = -32768;
    private final Map<LandTile, DataPoint> datamap = new ConcurrentHashMap<>();
    private final TileManager manager;
    private String rootDirectory;


    private RawDataConverter(TileManager manager, String root)
    {
      this.manager = manager;
      rootDirectory = root.endsWith("/") ? root : root + "/";
    }


    static void createNewDataFile(String filename)
    {
      createNewDataFile(DATA_FIELD.values(), filename);
    }


    static void createNewDataFile(DATA_FIELD fields[], String filename)
    {
      TileManager mgr = TileManager.createEmptyTileManager();
      RawDataConverter rdc = new RawDataConverter(mgr,
                                                  DEFAULT_ROOT);
      rdc.appendData(Arrays.asList(fields));
      mgr.retainAll(rdc.getCompleteTiles());
      System.out.printf("%d data tiles%n", mgr.dataTiles().size());
      writeToFile(mgr.dataTiles(), filename);
    }


    private void appendData(Collection<DATA_FIELD> fields)
    {
      final Map<DATA_FIELD, Boolean> done = new ConcurrentHashMap<>();
      for (final DATA_FIELD field : fields)
      {
        Runnable target = new Runnable()
        {
          @Override
          public void run()
          {
            readField(field);
            System.out.println(field + " is done reading");
            setField(field);
            System.out.println(field + " is done setting");
            done.put(field, true);
            System.out.println(field + " registered done");
          }
        };

        done.put(field, false);
        Thread t = new Thread(target);
        t.start();
      }

      int total = done.keySet().size();
      while (true)
      {
        int numDone = 0;
        for (DATA_FIELD field : done.keySet()) if (done.get(field)) numDone++;
        if (total - numDone < 1) break;
        if (total - numDone < 3)
        {
          System.out.printf("%d left of %d%n", total - numDone, total);
          for (DATA_FIELD field : done.keySet()) if (!done.get(field)) System.out.println(field);
          try
          {
            Thread.sleep(2000);
          }
          catch (InterruptedException e)
          {
          }
        }
      }
    }


    private Collection<LandTile> getCompleteTiles()
    {
      Collection<LandTile> tiles = new ArrayList<>();
      for (LandTile t : datamap.keySet()) if (datamap.get(t).hasAllData()) tiles.add(t);
      System.out.printf("got %d complete tiles%n", tiles.size());
      return tiles;
    }


    private void readField(DATA_FIELD field)
    {
      long start = System.currentTimeMillis();

      try (FileInputStream stream = new FileInputStream(new File(rootDirectory + field.filename)))
      {
        for (int row = 0; row < DATA_ROWS; row++)
        {
          if (row % 100 == 0) System.out.println(field + " - row: " + row);

          for (int col = 0; col < DATA_COLS; col++)
          {
            double lat = 90 - row * DATA_ARC_STEP;
            double lon = -180 + col * DATA_ARC_STEP;

            LandTile tile = manager.getTile(lon, lat);
            if (!datamap.containsKey(tile)) datamap.put(tile, new DataPoint());

            DataPoint point = datamap.get(tile);

            byte first = (byte) stream.read();
            byte second = (byte) stream.read();

            float data = twoByteToSignedInt(first, second);
            if (!(data == field.noDataVal)) point.putData(field, data);
          }
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      System.out.printf("reading and averaging field:%s took: %dms%n",
                        field, System.currentTimeMillis() - start);
    }


    private void setField(DATA_FIELD field)
    {
      int count = 0;
      for (LandTile tile : datamap.keySet())
      {
        long start = System.currentTimeMillis();
        DataPoint dataPoint = datamap.get(tile);
        if (dataPoint.hasAllData())
        {
          tile.putData(field.landTileField, dataPoint.getData(field) *
                                            DATA_FIELD.SCALE);
        }
        long end = System.currentTimeMillis();
        if (end - start > 1000)
        {
          System.out.println("taking a long time to put data into a single " +
                             "landtile: " + (end - start));
        }

        if (++count % 10000 == 0)
        {
          System.out.println("put data in " + count + " tiles for field " +
                             field);
        }
      }
    }


    private static int twoByteToSignedInt(byte first, byte second)
    {
      /* raster data is little-endian, two-byte signed ints */
      return (short) ((second << 8) | (first & 0xff));
    }


    public static Collection<LandTile> retainAndIDRegions(TileManager mgr, List<Region> regions)
    {
      Region last = regions.get(0);
      Collection<LandTile> tiles = new ArrayList<>();
      int count = 0;
      for (LandTile tile : mgr.allTiles())
      {
        if (last.containsMapPoint(tile.getCenter()))
        {
          tile.putData(LandTile.FIELD.COUNTRYID, last.getName().hashCode());
          tiles.add(tile);
          count++;
        }
        else
        {
          for (Region region : regions)
          {
            count++;
            if (region.containsMapPoint(tile.getCenter()))
            {
              last = region;
              tile.putData(LandTile.FIELD.COUNTRYID, last.getName().hashCode());
              tiles.add(tile);
              break;
            }
          }
        }
        if (tiles.size() % 1000 == 1) System.out.println("done placing " + tiles.size() + " tiles");
      }
      System.out.printf("took %d checks to place tiles%n", count);
      count = 0;
      int count2 = 0;
      for (LandTile t : mgr.allTiles())
      {
        if (tiles.contains(t))
        {
          count++;
        }
        else
        {
          count2++;
        }
      }
      System.out.println("contained : " + count + "; not contained " + count2);
      mgr.retainAll(tiles);
      return tiles;
    }


    /**
     DATA_FIELD enum describes the raw data set from which all land data is drawn.  Each field
     has an associated filename, no-data value (value that represents a placeholder in the source
     file) and an associated field in the WorldCell class.
     */
    public enum DATA_FIELD
    {
      BIOC1("bioc1.bil", NO_DATA_DEFAULT_CURR, CURRENT_ANNUAL_MEAN_TEMPERATURE),
      BIOC2("bioc2.bil", NO_DATA_DEFAULT_CURR, CURRENT_DIURNAL_RANGE),
      BIOC3("bioc3.bil", NO_DATA_DEFAULT_CURR, CURRENT_ISOTHERMALITY),
      BIOC4("bioc4.bil", NO_DATA_DEFAULT_CURR, CURRENT_TEMPERATURE_SEASONALITY),
      BIOC5("bioc5.bil", NO_DATA_DEFAULT_CURR, CURRENT_MAX_TEMPERATURE_OF_WARMEST_MONTH),
      BIOC6("bioc6.bil", NO_DATA_DEFAULT_CURR, CURRENT_MIN_TEMPERATURE_OF_COLDEST_MONTH),
      BIOC7("bioc7.bil", NO_DATA_DEFAULT_CURR, CURRENT_TEMPERATURE_ANNUAL_RANGE),
      BIOC8("bioc8.bil", NO_DATA_DEFAULT_CURR, CURRENT_MEAN_TEMPERATURE_OF_WETTEST_QUARTER),
      BIOC9("bioc9.bil", NO_DATA_DEFAULT_CURR, CURRENT_MEAN_TEMPERATURE_OF_DRIEST_QUARTER),
      BIOC10("bioc10.bil", NO_DATA_DEFAULT_CURR, CURRENT_MEAN_TEMPERATURE_OF_WARMEST_QUARTER),
      BIOC11("bioc11.bil", NO_DATA_DEFAULT_CURR, CURRENT_MEAN_TEMPERATURE_OF_COLDEST_QUARTER),
      BIOC12("bioc12.bil", NO_DATA_DEFAULT_CURR, CURRENT_ANNUAL_PRECIPITATION),
      BIOC13("bioc13.bil", NO_DATA_DEFAULT_CURR, CURRENT_PRECIPITATION_OF_WETTEST_MONTH),
      BIOC14("bioc14.bil", NO_DATA_DEFAULT_CURR, CURRENT_PRECIPITATION_OF_DRIEST_MONTH),
      BIOC15("bioc15.bil", NO_DATA_DEFAULT_CURR, CURRENT_PRECIPITATION_SEASONALITY),
      BIOC16("bioc16.bil", NO_DATA_DEFAULT_CURR, CURRENT_PRECIPITATION_OF_WETTEST_QUARTER),
      BIOC17("bioc17.bil", NO_DATA_DEFAULT_CURR, CURRENT_PRECIPITATION_OF_DRIEST_QUARTER),
      BIOC18("bioc18.bil", NO_DATA_DEFAULT_CURR, CURRENT_PRECIPITATION_OF_WARMEST_QUARTER),
      BIOC19("bioc19.bil", NO_DATA_DEFAULT_CURR, CURRENT_PRECIPITATION_OF_COLDEST_QUARTER),
      BIOP1("biop1.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_ANNUAL_MEAN_TEMPERATURE),
      BIOP2("biop2.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_DIURNAL_RANGE),
      BIOP3("biop3.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_ISOTHERMALITY),
      BIOP4("biop4.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_TEMPERATURE_SEASONALITY),
      BIOP5("biop5.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_MAX_TEMPERATURE_OF_WARMEST_MONTH),
      BIOP6("biop6.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_MIN_TEMPERATURE_OF_COLDEST_MONTH),
      BIOP7("biop7.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_TEMPERATURE_ANNUAL_RANGE),
      BIOP8("biop8.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_MEAN_TEMPERATURE_OF_WETTEST_QUARTER),
      BIOP9("biop9.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_MEAN_TEMPERATURE_OF_DRIEST_QUARTER),
      BIOP10("biop10.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_MEAN_TEMPERATURE_OF_WARMEST_QUARTER),
      BIOP11("biop11.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_MEAN_TEMPERATURE_OF_COLDEST_QUARTER),
      BIOP12("biop12.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_ANNUAL_PRECIPITATION),
      BIOP13("biop13.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_PRECIPITATION_OF_WETTEST_MONTH),
      BIOP14("biop14.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_PRECIPITATION_OF_DRIEST_MONTH),
      BIOP15("biop15.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_PRECIPITATION_SEASONALITY),
      BIOP16("biop16.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_PRECIPITATION_OF_WETTEST_QUARTER),
      BIOP17("biop17.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_PRECIPITATION_OF_DRIEST_QUARTER),
      BIOP18("biop18.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_PRECIPITATION_OF_WARMEST_QUARTER),
      BIOP19("biop19.bil", NO_DATA_DEFAULT_PROJ, PROJECTED_PRECIPITATION_OF_COLDEST_QUARTER),
      ELEV("altitude.bil", NO_DATA_DEFAULT_CURR, ELEVATION),;

      static final int SIZE = values().length;
      static final float SCALE = 0.1f;
      String filename;
      int noDataVal;
      LandTile.FIELD landTileField;


      DATA_FIELD(String filename, int noData, LandTile.FIELD landTileField)
      {
        this.landTileField = landTileField;
        this.filename = filename;
        noDataVal = noData;
      }


      int idx()
      {
        return ordinal();
      }
    }


    private static class DataPoint
    {

      float data[] = new float[DATA_FIELD.SIZE];
      int points[] = new int[DATA_FIELD.SIZE];
      boolean hasData[] = new boolean[DATA_FIELD.SIZE];


      public DataPoint()
      {
        Arrays.fill(data, 0);
        Arrays.fill(points, 0);
        Arrays.fill(hasData, false);
      }


      public void putData(DATA_FIELD field, float val)
      {
        hasData[field.idx()] = true;
        data[field.idx()] =
            (data[field.idx()] * points[field.idx()] + val) / (points[field.idx()] + 1);
        points[field.idx()]++;
      }


      public boolean hasData(DATA_FIELD field)
      {
        return hasData[field.idx()];
      }


      public boolean hasAllData()
      {
        for (boolean b : hasData) if (!b) return false;
        return true;
      }


      public float getData(DATA_FIELD field)
      {
        return data[field.idx()];
      }
    }
  }
}