package org.apache.jcs.admin;

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Keeps track of the number of bytes written to it, but doesn't write them
 * anywhere.
 */
public class CountingOnlyOutputStream
    extends OutputStream
{
  private int count;

  public void write( byte[] b ) throws IOException
  {
    count += b.length;
  }

  public void write( byte[] b, int off, int len ) throws IOException
  {
    count += len;
  }

  public void write( int b ) throws IOException
  {
    count++;
  }

  /**
   * The number of bytes that have passed through this stream.
   */
  public int getCount()
  {
    return this.count;
  }
}
