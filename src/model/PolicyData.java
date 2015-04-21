package model;

/**
 * Created by Lyncht on 4/20/15.
 */
public class PolicyData
{
  int id;
  String policy;
  String tag;
  String description;
  double likelyhood;
  String pro;
  String con;
  double gmo;
  double organic;
  double conventional;
  double corn;
  double soy;
  double rice;
  double wheat;
  double other;
  double level;

  /**
   *
   */
  public PolicyData() { /* Do Nothing */ }

  public int getId()
  {
    return id;
  }

  public void setId(int id)
  {
    this.id = id;
  }

  public String getPolicy()
  {
    return policy;
  }

  public void setPolicy(String policy)
  {
    this.policy = policy;
  }

  public String getTag()
  {
    return tag;
  }

  public void setTag(String tag)
  {
    this.tag = tag;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public double getLikelyhood()
  {
    return likelyhood;
  }

  public void setLikelyhood(double likelyhood)
  {
    this.likelyhood = likelyhood;
  }

  public String getPro()
  {
    return pro;
  }

  public void setPro(String pro)
  {
    this.pro = pro;
  }

  public String getCon()
  {
    return con;
  }

  public void setCon(String con)
  {
    this.con = con;
  }

  public double getGmo()
  {
    return gmo;
  }

  public void setGmo(double gmo)
  {
    this.gmo = gmo;
  }

  public double getOrganic()
  {
    return organic;
  }

  public void setOrganic(double organic)
  {
    this.organic = organic;
  }

  public double getConventional()
  {
    return conventional;
  }

  public void setConventional(double conventional)
  {
    this.conventional = conventional;
  }

  public double getCorn()
  {
    return corn;
  }

  public void setCorn(double corn)
  {
    this.corn = corn;
  }

  public double getSoy()
  {
    return soy;
  }

  public void setSoy(double soy)
  {
    this.soy = soy;
  }

  public double getRice()
  {
    return rice;
  }

  public void setRice(double rice)
  {
    this.rice = rice;
  }

  public double getWheat()
  {
    return wheat;
  }

  public void setWheat(double wheat)
  {
    this.wheat = wheat;
  }

  public double getOther()
  {
    return other;
  }

  public void setOther(double other)
  {
    this.other = other;
  }

  public double getLevel()
  {
    return level;
  }

  public void setLevel(double level)
  {
    this.level = level;
  }

}
