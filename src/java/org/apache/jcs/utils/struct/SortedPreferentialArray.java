package org.apache.jcs.utils.struct;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  // prefer large means that the smallest will be removed when full.
  private boolean preferLarge = true;
  private int maxSize = 0;
  private int curSize = 0;
  private Comparable[] array;

  private int insertCnt = 0;

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
    try
    {
      int nLar = findNearestLargerOrEqualPosition(obj);
      if (log.isDebugEnabled())
      {
        log.debug("nLar = " + nLar + " obj = " + obj);
      }

      if (nLar == curSize)
      {
        // this next check should be unnecessary
        // findNearestLargerPosition should only return the curSize if there is
        // room left.  Check to be safe
        if (curSize < maxSize)
        {
          array[nLar] = obj;
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
        } // end if not full
      }

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
        if (isFull)
        {
          // is full, prefer larger, remove smallest by shifting left
          int pnt = nLar - 1; // set iteration stop point
          for (int i = 0; i < pnt; i++)
          {
            array[i] = array[i + 1];
          }
          // use nLar-1 for insertion point
          array[nLar - 1] = obj;
          if (log.isDebugEnabled())
          {
            log.debug("Inserted object at " + (nLar - 1));
          }
        }
        else
        {
          // not full, shift right from spot
          int pnt = nLar; // set iteration stop point
          for (int i = curSize; i > pnt; i--)
          {
            array[i] = array[i - 1];
          }
          // use nLar-1 for insertion point
          array[nLar] = obj;
          curSize++;
          if (log.isDebugEnabled())
          {
            log.debug("Inserted object at " + (nLar));
          }
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
        for (int i = curSize; i > pnt; i--)
        {
          array[i] = array[i - 1];
        }
        array[nLar] = obj;
        if (log.isDebugEnabled())
        {
          log.debug("Inserted object at " + nLar);
        }
      }

      if (log.isDebugEnabled())
      {
        log.debug(this.dumpArray());
      }
    }
    catch (Exception e)
    {
      log.error("Insertion problem" + this.dumpArray(), e);
    }

    insertCnt++;
    if (insertCnt % 100 == 0)
    {
      if (log.isInfoEnabled())
      {
        log.info(this.dumpArray());
      }
    }

  } // end insert

  /**
   * Determines whether the preference is for large or small.
   *
   * @param pref boolean
   */
  public void setPreferLarge(boolean pref)
  {
    preferLarge = pref;
  }

  /**
   * Returns and removes the nearer larger or equal object from the aray.
   *
   * @param obj Comparable
   * @return Comparable
   */
  public Comparable takeNearestLargerOrEqual(Comparable obj)
  {
    Comparable retVal = null;
    try
    {
      int pos = findNearestOccupiedLargerOrEqualPosition(obj);
      if (pos == -1)
      {
        return null;
      }

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
    }
    catch (Exception e)
    {
      log.error("Take problem" + this.dumpArray(), e);
    }
    return retVal;
  }

  /**
   * This determines the position in the array that is occupied by an object
   * that is larger or equal to the argument.  If none exists, -1 is
   * returned.
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
    int pos = findNearestLargerOrEqualPosition(obj);
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

  /**
   * This method determines the position where an insert should take place
   * for a given object.  With some additional checking, this can also be used
   * to find an object matching or greater than the argument.
   *
   * If the array is not full and the current object is larger than all
   * the rest the first open slot at the end will be returned.
   *
   * If the object is larger than the largest and it is full, it
   * will return the last position.
   *
   * If the array is empty, the first spot is returned.
   *
   * If the object is smaller than all the rests, the first position is
   * returned.  The caller must decide what to do given the preference.
   *
   * Returns the position of the object nearest to or equal to the larger object.
   *
   * If you want to find the takePosition, you have to calculate it.
   * findNearestOccupiedLargerOrEqualPosition will calculate this for you
   *
   * @param obj Comparable
   * @return int
   */
  private int findNearestLargerOrEqualPosition(Comparable obj)
  {

    // do nothing if a null was passed in
    if (obj == null)
    {
      return -1;
    }

    // return the first spot if the array is empty
    if (curSize <= 0)
    {
      return 0;
    }

    // mark the numer to be returned, the greaterPos as unset
    int greaterPos = -1;
    // prepare for a binary search
    int curPos = (curSize - 1) / 2;
    int prevPos = -1;

    try
    {
      // set the loop exit flag to false
      boolean done = false;

      // check the ends
      // return insert position 0 if obj is smaller
      // than the smallest.  the caller can determine what to
      // do with this, depending on the preference setting
      if (obj.compareTo(getSmallest()) <= 0)
      {
        //LESS THAN OR EQUAL TO SMALLEST
        if (log.isDebugEnabled())
        {
          log.debug(obj + " is smaller than or equal to " + getSmallest());
        }
        greaterPos = 0;
        done = true;
        //return greaterPos;
      }
      else
      {
        //GREATER THAN SMALLEST
        if (log.isDebugEnabled())
        {
          log.debug(obj + " is bigger than " + getSmallest());
        }

        // return the largest position if obj is larger
        // than the largest.  the caller can determine what to
        // do with this, depending on the preference setting
        if (obj.compareTo(getLargest()) >= 0)
        {
          if (curSize == maxSize)
          {
            // there is no room left in the array, return the last spot
            greaterPos = curSize - 1;
            done = true;
          }
          else
          {
            // there is room left in the array
            greaterPos = curSize;
            done = true;
          }
        }
        else
        {
          // the obj is less than the largest, so we know that the last
          // item is larger
          greaterPos = curSize - 1;
        }
      }

      ///////////////////////////////////////////////////////////////////////
      // begin binary search for insertion spot
      while (!done)
      {
        if (log.isDebugEnabled())
        {
          log.debug("\n curPos = " + curPos + "; greaterPos = " + greaterPos +
                    "; prevpos = " + prevPos);
        }

        // get out of loop if we have come to the end or passed it
        if (curPos == prevPos || curPos >= curSize)
        {
          done = true;
          break;
        }
        else

        // EQUAL TO
        // object at current position is equal to the obj, use this,
        // TODO could avoid some shuffling if I found a lower pos.
        if (array[curPos].compareTo(obj) == 0)
        {
          if (log.isDebugEnabled())
          {
            log.debug(array[curPos] + " is equal to " + obj);
          }
          greaterPos = curPos;
          done = true;
          break;
        }
        else

        // GREATER THAN
        // array object at current position is greater than the obj, go left
        if (array[curPos].compareTo(obj) > 0)
        {
          if (log.isDebugEnabled())
          {
            log.debug(array[curPos] + " is greater than " + obj);
          }
          // set the smallest greater equal to the current position
          greaterPos = curPos;
          // set the current position to
          // set the previous position to the current position
          int newPos = Math.min( curPos, (curPos + prevPos) / 2 );
          prevPos = curPos;
          curPos = newPos;
        }
        else

        // LESS THAN
        // the object at the current position is smaller, go right
        if (array[curPos].compareTo(obj) < 0)
        {
          if (log.isDebugEnabled())
          {
            log.debug(array[curPos] + " is less than " + obj);
          }
          if ( (greaterPos != -1) && greaterPos - curPos < 0)
          {
            done = true;
            break; //return greaterPos;
          }
          else
          {
            int newPos = 0;
            if (prevPos > curPos)
            {
              newPos = Math.min( (curPos + prevPos) / 2, curSize);
            }
            else
            if (prevPos == -1)
            {
              newPos = Math.min( (curSize + curPos) / 2, curSize);
            }
            prevPos = curPos;
            curPos = newPos;
          }
        }
      } // end while
      ///////////////////////////////////////////////////////////////////////

      if (log.isDebugEnabled())
      {
        log.debug("Greater Position is [" + greaterPos + "]" +
                  " array[greaterPos] [" + array[greaterPos] + "]");
      }
    }
    catch (Exception e)
    {
      log.error("\n curPos = " + curPos + "; greaterPos = " + greaterPos +
                "; prevpos = " + prevPos + " " + this.dumpArray(), e);
    }

    return greaterPos;
  }

  /**
   * Removes the item from the array at the specified position.  The remaining
   * items to the right are shifted left.
   * @param position int
   */
  private void remove(int position)
  {
    try
    {
      // suffle left from removal point
      int end = curSize - 1;
      for (int i = position; i < end; i++)
      {
        array[i] = array[i + 1];
      }
      curSize--;
    }
    catch (Exception e)
    {
      log.error("Remove problem" + this.dumpArray(), e);
    }

  }

  /**
   * Debugging method to return a human readable display of array data.
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

  /**
   * Simple testing program.
   *
   * @param args String[]
   */
  public static void main(String args[])
  {

    SortedPreferentialArray array = new SortedPreferentialArray(25);
    //array.setPreferLarge( false );
    array.setPreferLarge( true );
    String[] elem =
        {
        "10", "11", "01", "02", "03", "04", "05", "08", "07",
        "06", "09", "12", "13", "15", "14", "20", "25", "29", "28", "16", "17",
        "96", "00", "72", "39", "55", "44", "26", "22", "59", "38", "16", "27"};

    for (int i = 0; i < elem.length; i++)
    {
      array.add(elem[i]);
      System.out.println(array.dumpArray());
    }

    //String t = "01";
    //System.out.print(t.compareTo("10"));

  }

}
