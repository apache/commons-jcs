package org.apache.jcs.utils.struct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This maintains a sorted array with a preferential replacement policy when
 * full.
 *
 * Insertion time is n, search is log(n)
 *
 *
 * Clients must manage thread safety.
 *
 */
public class SortedPreferentialArray
{

  private static final Log log =
      LogFactory.getLog(SortedPreferentialArray.class);

  // prefer large means that the smalles will be removed when full.
  private boolean preferLarge = true;
  private int maxSize = 0;
  private int curSize = 0;
  private Comparable[] array;

  /**
   * Consruct the array with the maximum size.
   *
   * @param maxSize int
   */
  public SortedPreferentialArray(int maxSize)
  {
    this.maxSize = maxSize;
    array = new Comparable[maxSize];
  }

  /**
   * If the array is full this will remove the smallest if preferLarge==true and
   * if obj is bigger, or the largest if preferLarge=false and obj is smaller
   * than the largest.
   *
   *
   * @param obj Object
   */
  public void add(Comparable obj)
  {
    if (curSize < maxSize)
    {
      insert(obj);
    }
    else
    {
      if (preferLarge)
      {
        // insert if obj is larger than the smallest
        Comparable sma = getSmallest();
        if (obj.compareTo(sma) > 0)
        {
          insert(obj);
        }
        else
        {
          if (log.isDebugEnabled())
          {
            log.debug("New object is smaller than smallest");
          }
          return;
        }
      }
      else
      if (!preferLarge)
      {
        Comparable lar = getLargest();
        // insert if obj is smaller than the largest
        if (obj.compareTo(lar) > 0)
        {
          if (log.isDebugEnabled())
          {
            log.debug("New object is largerer than largest");
          }
          return;
        }
        else
        {
          insert(obj);
        }
      }
    }
  }

  public Comparable getLargest()
  {
    int num = curSize - 1;
    if (num < 0)
    {
      num = 0;
    }
    return array[num];
  }

  public Comparable getSmallest()
  {
    return array[0];
  }

  /**
   * Insert looks for the nearest largest.  It then determines which way
   * to shuffle depending on the preference.
   *
   * @param obj Comparable
   */
  private void insert(Comparable obj)
  {
    int nLar = findNearestLargerPositionForInsert(obj);

    if (nLar == curSize)
    {
      // this next check should be unnecessary
      // findNearestLargerPosition should only return the curSize if there is
      // room left.  Check to be safe
      if (curSize < maxSize)
      {
        array[curSize] = obj;
        curSize++;
        if (log.isDebugEnabled())
        {
          log.debug(this.dumpArray());
        }
        if (log.isDebugEnabled())
        {
          log.debug("Inserted object at the end of the array");
        }
        return;
      }
    } // end if not full

    boolean isFull = false;
    if (curSize == maxSize)
    {
      isFull = true;
    }
    // The array is full, we must replace
    // remove smallest or largest to determine whether to
    // shuffle left or right to insert
    if (preferLarge)
    {
      // prefer larger, remove smallest by shifting left
      // use nLar-1 for insertion point
      int pnt = nLar;
      if (!isFull)
      {
        pnt = nLar + 1;
      }
      for (int i = 0; i < pnt; i++)
      {
        array[i] = array[i + 1];
      }
      array[nLar - 1] = obj;
      if (log.isDebugEnabled())
      {
        log.debug("Inserted object at " + (nLar - 1));
      }
    }
    else
    {
      // prefer smaller, remove largest by shifting right
      // use nLar for insertion point
      int pnt = nLar + 1;
      if (!isFull)
      {
        pnt = nLar;
      }
      for (int i = curSize - 1; i > pnt; i--)
      {
        array[i] = array[i - 1];
      }
      array[nLar] = obj;
      if (log.isDebugEnabled())
      {
        log.debug("Inserted object at " + nLar);
      }
    }

    //} // end if full

    // TODO handle case where the new item is less than the largest and the
    // array is not full.

    if (log.isDebugEnabled())
    {
      log.debug(this.dumpArray());
    }

  } // end insert

  /**
   * Determines whether the preference is for large or small.
   *
   *
   * @param pref boolean
   */
  public void setPreferLarge(boolean pref)
  {
    preferLarge = pref;
  }

  /**
   * Returns and removes the nearerLarger from the aray
   *
   * @param obj Comparable
   * @return Comparable
   */
  public Comparable takeNearestLargerOrEqual(Comparable obj)
  {
    int pos = findNearestOccupiedLargerOrEqualPosition(obj);
    if (pos == -1)
    {
      return null;
    }

    Comparable retVal = null;
    try
    {
      retVal = array[pos];
      remove(pos);
    }
    catch (Exception e)
    {
      log.error(e);
    }

    if (log.isDebugEnabled())
    {
      log.debug("obj = " + obj + " || retVal = " + retVal);
    }

    return retVal;
  }

  /**
   *
   * @param obj Object
   * @return Object
   */
  private int findNearestOccupiedLargerOrEqualPosition(Comparable obj)
  {
    if (curSize == 0)
    {
      // nothing in the array
      return -1;
    }

    // this gives us an insert position.
    int pos = findNearestLargerPositionForInsert(obj);
    // see if the previous will do to handle the empty insert spot position
    if (pos == curSize)
    { //&& curSize < maxSize ) {
      // pos will be > 0 if it equals curSize, we check for this above.
      if (obj.compareTo(array[pos - 1]) <= 0)
      {
        pos = pos - 1;
      }
      else
      {
        pos = -1;
      }
    }
    return pos;
  }

  private void remove(int position)
  {
    // suffle left from removal point
    for (int i = position; i < curSize - 1; i++)
    {
      array[i] = array[i + 1];
    }
    curSize--;
  }

  /**
   * If the array is not full and the current object is larger than all
   * the rest the first open slot at the end will be returned.
   *
   * If you want to find the takePosition, you have to calculate it.
   *
   * If the object is larger than the largest and it is full, it
   * will return the last position.
   *
   * Returns the position of the nearest to or equal to the larger object.
   * Returns -1 if the object is null.
   *
   * @param obj Comparable
   * @return int
   */
  private int findNearestLargerPositionForInsert(Comparable obj)
  {

    if (obj == null)
    {
      return -1;
    }

    if (curSize <= 0)
    {
      return 0;
    }

    int greaterPos = -1;

    try
    {

      int curPos = (curSize - 1) / 2;
      int prevPos = -1;
      boolean done = false;

      // check the ends
      if (obj.compareTo(getSmallest()) <= 0)
      {
        greaterPos = 0;
        done = true;
      }

      if (obj.compareTo(getLargest()) >= 0)
      {
        if (curSize == maxSize)
        {
          greaterPos = curSize - 1;
          done = true;
        }
        else
        {
          greaterPos = curSize;
          done = true;
        }
      }
      else
      {
        greaterPos = curSize - 1;
      }

      while (!done)
      {
        if (log.isDebugEnabled())
        {
          log.debug("curPos = " + curPos + "; greaterPos = " + greaterPos +
                    "; prevpos = " + prevPos);
        }

        if (curPos == prevPos)
        {
          done = true;
          break;
        }
        // object at current position is equakl to the obj, use this,
        // TODO could avoid some shuffling if I found a lower pos.
        if (array[curPos].compareTo(obj) == 0)
        {
          greaterPos = curPos;
          done = true;
          break;
        }
        // object at current position is greater than the obj, go left
        if (array[curPos].compareTo(obj) >= 0)
        {
          // set the smalelst greater equal to the current position
          greaterPos = curPos;
          // set the current position to
          // set the previous position to the current position
          int newPos = curPos + prevPos / 2;
          prevPos = curPos;
          curPos = newPos;
        }
        else
        {
          // the object at the current position is smaller, go right
          if ( ( (greaterPos != -1) && greaterPos - curPos < 0))
          {
            done = true;
            break; //return greaterPos;
          }
          else
          {
            int newPos = 0;
            if (prevPos > curPos)
            {
              newPos = (curPos + prevPos) / 2;
            }
            else
            if (prevPos == -1)
            {
              newPos = (curSize + curPos) / 2;
            }
            prevPos = curPos;
            curPos = newPos;
          }
        }
      } // end while

      if (log.isDebugEnabled())
      {
        log.debug("Greater Position is [" + greaterPos + "]");
        log.debug("array[greaterPos] [" + array[greaterPos] + "]");
      }
    }
    catch (Exception e)
    {
      log.error(this.dumpArray(), e);
    }

    return greaterPos;
  }

  /**
   * Debugging method
   */
  private String dumpArray()
  {
    StringBuffer buf = new StringBuffer();
    buf.append("\n ---------------------------");
    buf.append("\n curSize = " + curSize);
    buf.append("\n array.length = " + array.length);
    buf.append("\n ---------------------------");
    buf.append("\n Dump:");
    for (int i = 0; i < curSize; i++)
    {
      buf.append("\n " + i + "=" + array[i]);
    }
    return buf.toString();
  }

}
