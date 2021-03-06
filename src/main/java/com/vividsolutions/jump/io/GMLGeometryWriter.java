/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jts.geom.*;
import java.io.*;

/**
 * Writes or creates a formatted string containing the GML
 * representation of a JTS Geometry.
 * Supports a user-defined line prefix and a user-defined maximum number of coordinates per line.
 * Indents components of Geometries to provide a nicely-formatted representation.
 */
public class GMLGeometryWriter
{

  /**
   *  Returns a <code>String</code> of repeated characters.
   *
   *@param  ch     the character to repeat
   *@param  count  the number of times to repeat the character
   *@return        a <code>String</code> of characters
   */
  private static String stringOfChar(char ch, int count) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < count; i++) {
      buf.append(ch);
    }
    return buf.toString();
  }


  private final int INDENT_SIZE = 2;
  // these could be make settable
  private static final String coordinateSeparator = ",";
  private static final String tupleSeparator = " ";

  private String linePrefix = null;
  private int maxCoordinatesPerLine = 10;
  private String srsName = null;
  private String gid = null;

  public GMLGeometryWriter() {
  }

  public void setLinePrefix(String linePrefix)
  {
    this.linePrefix = linePrefix;
  }

  /**
   * Sets the <code>srsName</code> attribute to be output on the Geometry element.
   * If <code>null</code> no attribute will be output.
   * @param srsName
   */
  public void setSRSName(String srsName)
  {
    this.srsName = srsName;
  }

  /**
   * Sets the <code>gid</code> attribute to be output on the Geometry element.
   * If <code>null</code> no attribute will be output.
   * @param srsName
   */
  public void setGID(String gid)
  {
    this.gid = gid;
  }

  public void setMaximumCoordinatesPerLine(int maxCoordinatesPerLine)
  {
    if (maxCoordinatesPerLine <= 0) {
      maxCoordinatesPerLine = 1;
      return;
    }
    this.maxCoordinatesPerLine = maxCoordinatesPerLine;
  }

  public String write(Geometry geom)
  {
    StringBuffer buf = new StringBuffer();
    write(geom, buf);
    return buf.toString();
  }

  public void write(Geometry geometry, Writer writer)
    throws IOException
  {
    writer.write(write(geometry));
  }



  /**
   * Generates the GML representation of a JTS Geometry.
   * @param g Geometry to output
   */
  public void write(Geometry g, StringBuffer buf)
  {
    writeGeometry(g, attributeString(), 0, buf);
  }

  /**
   * Generates the GML representation of a JTS Geometry.
   * @param g Geometry to output
   */
  private void writeGeometry(Geometry g, String attributes, int level, StringBuffer buf) {
    /*
     * order is important in this if-else list.
     * E.g. homogeneous collections need to come before GeometryCollection
    */
      if (g instanceof Point) {
          writePoint((Point) g, attributes, level, buf);
      } else if (g instanceof LinearRing) {
          writeLinearRing((LinearRing) g, attributes, level, buf);
      } else if (g instanceof LineString) {
          writeLineString((LineString) g, attributes, level, buf);
      } else if (g instanceof Polygon) {
          writePolygon((Polygon) g, attributes, level, buf);
      } else if (g instanceof MultiPoint) {
          writeMultiPoint((MultiPoint) g, attributes, level, buf);
      } else if (g instanceof MultiLineString) {
          writeMultiLineString((MultiLineString) g, attributes, level, buf);
      } else if (g instanceof MultiPolygon) {
          writeMultiPolygon((MultiPolygon) g, attributes, level, buf);
      } else if (g instanceof GeometryCollection) {
        writeGeometryCollection((GeometryCollection) g, attributes, level, buf);
      }
      // throw an error for an unknown type?
  }

  private void startLine(StringBuffer buf, int level, String text)
  {
    if (linePrefix != null) buf.append(linePrefix);
    buf.append(stringOfChar(' ', INDENT_SIZE * level));
    buf.append(text);
  }

  private String geometryTag(String geometryName, String attributes)
  {
    StringBuffer buf = new StringBuffer();
    buf.append("<gml:");
    buf.append(geometryName);
    if (attributes != null && attributes.length() > 0) {
      buf.append(" ");
      buf.append(attributes);
    }
    buf.append(">");
    return buf.toString();
  }

  private String attributeString()
  {
    StringBuffer buf = new StringBuffer();
    if (gid != null) {
      buf.append(" gid='");
      buf.append(gid);
      buf.append("'");
    }
    if (srsName != null) {
      buf.append(" srsName='");
      buf.append(srsName);
      buf.append("'");
    }
    return buf.toString();
  }

  //<gml:Point><gml:coordinates>1195156.78946687,382069.533723461</gml:coordinates></gml:Point>
  private void writePoint(Point p, String attributes, int level, StringBuffer buf) {
      startLine(buf, level, geometryTag("Point", attributes) + "\n");
      write(new Coordinate[] { p.getCoordinate() }, level + 1, buf);
      startLine(buf, level, "</gml:Point>\n");
  }

  //<gml:LineString><gml:coordinates>1195123.37289257,381985.763974674 1195120.22369473,381964.660533343 1195118.14929823,381942.597718511</gml:coordinates></gml:LineString>
  private void writeLineString(LineString ls, String attributes, int level, StringBuffer buf) {
    startLine(buf, level, geometryTag("LineString", attributes) + "\n");
    write(ls.getCoordinates(), level + 1, buf);
    startLine(buf, level, "</gml:LineString>\n");
  }

  //<gml:LinearRing><gml:coordinates>1226890.26761027,1466433.47430292 1226880.59239079,1466427.03208053...></coordinates></gml:LinearRing>
  private void writeLinearRing(LinearRing lr, String attributes, int level, StringBuffer buf) {
    startLine(buf, level, geometryTag("LinearRing", attributes) + "\n");
    write(lr.getCoordinates(), level + 1, buf);
    startLine(buf, level, "</gml:LinearRing>\n");
  }

  private void writePolygon(Polygon p, String attributes, int level, StringBuffer buf) {
    startLine(buf, level, geometryTag("Polygon", attributes) + "\n");

    startLine(buf, level, "  <gml:outerBoundaryIs>\n");
    writeLinearRing((LinearRing) p.getExteriorRing(), null, level + 1, buf);
    startLine(buf, level, "  </gml:outerBoundaryIs>\n");

    for (int t = 0; t < p.getNumInteriorRing(); t++) {
      startLine(buf, level, "  <gml:innerBoundaryIs>\n");
      writeLinearRing((LinearRing) p.getInteriorRingN(t), null, level + 1, buf);
      startLine(buf, level, "  </gml:innerBoundaryIs>\n");
    }

    startLine(buf, level, "</gml:Polygon>\n");
  }

  private void writeMultiPoint(MultiPoint mp, String attributes, int level, StringBuffer buf) {
    startLine(buf, level, geometryTag("MultiPoint", attributes) + "\n");
    for (int t = 0; t < mp.getNumGeometries(); t++) {
      startLine(buf, level, "  <gml:pointMember>\n");
      writePoint((Point) mp.getGeometryN(t), null, level + 1, buf);
      startLine(buf, level, "  </gml:pointMember>\n");
    }
    startLine(buf, level, "</gml:MultiPoint>\n");
  }

  private void writeMultiLineString(MultiLineString mls, String attributes, int level, StringBuffer buf) {
    startLine(buf, level, geometryTag("MultiLineString", attributes) + "\n");
    for (int t = 0; t < mls.getNumGeometries(); t++) {
      startLine(buf, level, "  <gml:lineStringMember>\n");
      writeLineString((LineString) mls.getGeometryN(t), null, level + 1, buf);
      startLine(buf, level, "  </gml:lineStringMember>\n");
    }
    startLine(buf, level, "</gml:MultiLineString>\n");
  }

  private void writeMultiPolygon(MultiPolygon mp, String attributes, int level, StringBuffer buf) {
    startLine(buf, level, geometryTag("MultiPolygon", attributes) + "\n");
    for (int t = 0; t < mp.getNumGeometries(); t++) {
      startLine(buf, level, "  <gml:polygonMember>\n");
      writePolygon((Polygon) mp.getGeometryN(t), null, level + 1, buf);
      startLine(buf, level, "  </gml:polygonMember>\n");
    }
    startLine(buf, level, "</gml:MultiPolygon>\n");
  }

  private void writeGeometryCollection(GeometryCollection gc, String attributes, int level, StringBuffer buf) {
    startLine(buf, level, geometryTag("MultiGeometry", attributes) + "\n");
    for (int t = 0; t < gc.getNumGeometries(); t++) {
      startLine(buf, level, "  <gml:geometryMember>\n");
      writeGeometry(gc.getGeometryN(t), null, level + 1, buf);
      startLine(buf, level, "  </gml:geometryMember>\n");
    }
    startLine(buf, level, "</gml:MultiGeometry>\n");
  }

  /**
   * Takes a list of coordinates and converts it to GML.<br>
   * 2d and 3d aware.
   * Terminates the coordinate output with a newline.
   *@param cs array of coordinates
   */
  private void write(Coordinate[] coords, int level, StringBuffer buf) {
    startLine(buf, level, "<gml:coordinates>");
    int dim = 2;

    if (coords.length > 0) {
      if (!(Double.isNaN(coords[0].z)))
         dim = 3;
    }

    boolean isNewLine = false;
    for (int i = 0; i < coords.length; i++) {
      if (isNewLine) {
        startLine(buf, level, "  ");
        isNewLine = false;
      }
      if (dim == 2) {
        buf.append(coords[i].x);
        buf.append(coordinateSeparator);
        buf.append(coords[i].y);
      } else if (dim == 3) {
        buf.append(coords[i].x);
        buf.append(coordinateSeparator);
        buf.append(coords[i].y);
        buf.append(coordinateSeparator);
        buf.append(coords[i].z);
      }
      buf.append(tupleSeparator);

      // break output lines to prevent them from getting too long
      if ((i + 1) % maxCoordinatesPerLine == 0 && i < coords.length - 1) {
        buf.append("\n");
        isNewLine = true;
      }
    }

    buf.append("</gml:coordinates>\n");
  }
}
