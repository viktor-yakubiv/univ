public class Page 
{
  public int id;
  public int physical;
  public byte R;
  public byte M;
  public int inMemTime;
  public int lastTouchTime;
  public long high;
  public long low;

  public Page( int id, int physical, byte R, byte M, int inMemTime, int lastTouchTime, long high, long low ) 
  {
    this.id = id;
    this.physical = physical;
    this.R = R;
    this.M = M;
    this.inMemTime = inMemTime;
    this.lastTouchTime = lastTouchTime;
    this.high = high;
    this.low = low;
  }

  @Override
  public String toString() {
    return "Page {" +
            "\n  id: " + id +
            "\n  physical: " + physical +
            "\n  R: " + R +
            "\n  M: " + M +
            "\n  inMemTime: " + inMemTime +
            "\n  lastTouchTime: " + lastTouchTime +
            "\n  high: " + high +
            "\n  low: " + low +
            "\n}";
  }
}
