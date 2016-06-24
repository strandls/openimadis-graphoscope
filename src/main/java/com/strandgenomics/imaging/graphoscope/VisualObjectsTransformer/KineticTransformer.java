/*
 * KineticTransform.java
 *
 * Product:  faNGS
 * Next Generation Sequencing
 *
 * Copyright 2007-2012, Strand Life Sciences
 * 5th Floor, Kirloskar Business Park, 
 * Bellary Road, Hebbal,
 * Bangalore 560024
 * India
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Strand Life Sciences., ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Strand Life Sciences.
 */
package com.strandgenomics.imaging.graphoscope.VisualObjectsTransformer;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D.Double;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.strandgenomics.imaging.icore.vo.Arrow;
import com.strandgenomics.imaging.icore.vo.Circle;
import com.strandgenomics.imaging.icore.vo.Ellipse;
import com.strandgenomics.imaging.icore.vo.GeometricPath;
import com.strandgenomics.imaging.icore.vo.LineSegment;
import com.strandgenomics.imaging.icore.vo.Polygon;
import com.strandgenomics.imaging.icore.vo.Rectangle;
import com.strandgenomics.imaging.icore.vo.TextBox;
import com.strandgenomics.imaging.icore.vo.VisualObject;
import com.strandgenomics.imaging.icore.vo.VisualObjectType;

/**
 * Visual object transformer to and from KineticJS objects. Instances are
 * thread-safe.
 * 
 * @author navneet
 * 
 */
public class KineticTransformer  extends VisualObjectTransformer<Map<String, Object>>  {


    /**
     * Logger instance to use
     */
    private Logger logger;

    /**
     * Create new kinetic transformer
     */
    public KineticTransformer() {
        logger = Logger.getLogger("com.strandgenomics.imaging.iserver.impl.web.io");
    }    

	@Override
	public VisualObjectType getType(Map<String, Object> object) {
        String typeString = (String) object.get("type");
        if (typeString.equalsIgnoreCase("Rect"))
            return VisualObjectType.RECTANGLE;
        else if (typeString.equalsIgnoreCase("Ellipse"))
            return VisualObjectType.ELLIPSE;
        else if (typeString.equalsIgnoreCase("Circle"))
            return VisualObjectType.CIRCLE;
        else if (typeString.equalsIgnoreCase("Text"))
            return VisualObjectType.TEXT;
        else if (typeString.equalsIgnoreCase("Path"))
            return VisualObjectType.PATH;
        else if (typeString.equalsIgnoreCase("Line"))
            return VisualObjectType.LINE;  
        else if (typeString.equalsIgnoreCase("Polygon"))
            return VisualObjectType.POLYGON;   
        /*else if (typeString.equalsIgnoreCase("Arrow"))
            return VisualObjectType.ARROW;   */      
        return null;
	}

	/**
	 * In kineticjs x,y is the center of ellipse
	 * and radiusX and radiusY are radius in X&Y directions
	 */
	@Override
	protected Map<String, Object> encodeEllipse(Ellipse ellipse) {
		
        Double bounds = ellipse.getBounds();
        Map<String, Object> kineticObject = getCommonKineticObject(ellipse);
        kineticObject.put("type", "ellipse");
        kineticObject.put("left", bounds.x);
        kineticObject.put("top",bounds.y);
        kineticObject.put("width", bounds.width);
        kineticObject.put("height", bounds.height);
        kineticObject.put("rotation", ellipse.getRotationInDegrees());
        
        kineticObject.put("rx", bounds.width/2);
        kineticObject.put("ry", bounds.height/2); 
        
        kineticObject.put("strokeLineCap","butt");
        kineticObject.put("strokeLineJoin", "miter");
        kineticObject.put("originY", "top");
        kineticObject.put("originX", "left");
        kineticObject.put("fill", "");
        
        logger.logp(Level.INFO, "KineticTransformer", "encodeEllipse", "radiusX="+bounds.width/2+" radiusY="+bounds.width/2+" x="+(bounds.x+bounds.width)+" y="+(bounds.y+bounds.height));
        return kineticObject;

	}

	/**
	 * In kineticjs x,y is the center of ellipse
	 * and radiusX and radiusY are radius in X&Y directions
	 */
	@Override
	protected VisualObject decodeEllipse(Map<String, Object> ellipseData) {

        double width = getDouble(ellipseData.get("width"));
        double height = getDouble(ellipseData.get("height"));
        double x = getDouble(ellipseData.get("left"));
        double y = getDouble(ellipseData.get("top"));
        
		logger.logp(Level.INFO, "KineticTransformer", "decodeEllipse", "width="+width+" height="+height+" x="+x+" y="+y);
        Ellipse ellipse;
        if (ellipseData.containsKey("custom")) {
            @SuppressWarnings("unchecked")
			Map<String, Object> customData = (Map<String, Object>) ellipseData.get("custom");
            int ID = Integer.parseInt(customData.get("objectid").toString());
            ellipse = new Ellipse(ID, x, y, width, height);
        } else {
            ellipse = new Ellipse(x, y, width, height);
        }
        ellipse.setType(VisualObjectType.ELLIPSE);
        addCommonAttributes(ellipse, ellipseData);
        
        return ellipse;
        
	}

	@Override
	protected Map<String, Object> encodeCircle(Circle circle) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected VisualObject decodeCircle(Map<String, Object> serialized) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Map<String, Object> encodeRectangle(Rectangle rectangle) {
        Double bounds = rectangle.getBounds();
        Map<String, Object> kineticObject = getCommonKineticObject(rectangle);
        kineticObject.put("type", "rect");
        kineticObject.put("left", bounds.x);
        kineticObject.put("top", bounds.y);
        kineticObject.put("width", bounds.width);
        kineticObject.put("height", bounds.height);
        kineticObject.put("rotation", rectangle.getRotation());
        
        kineticObject.put("strokeLineCap","butt");
        kineticObject.put("strokeLineJoin", "miter");
        kineticObject.put("originY", "top");
        kineticObject.put("originX", "left");
        kineticObject.put("fill", "");
		return kineticObject;
	}

	@Override
	protected VisualObject decodeRectangle(Map<String, Object> rectData) {
        double width = getDouble(rectData.get("width"));
        double height = getDouble(rectData.get("height"));
        double x = getDouble(rectData.get("left"));
        double y = getDouble(rectData.get("top"));
        Rectangle rectangle = new Rectangle(x, y, width, height);
        rectangle.setType(VisualObjectType.RECTANGLE);
        addCommonAttributes(rectangle, rectData);
        return rectangle;
	}

	@Override
	protected Map<String, Object> encodeText(TextBox object) {
        Double bounds = object.getBounds();
        Map<String, Object> kineticObject = new HashMap<String, Object>();
        kineticObject.put("type", "text");
        kineticObject.put("left", bounds.x);
        kineticObject.put("top", bounds.y);
        kineticObject.put("width", bounds.width);
        kineticObject.put("height", bounds.height);
        kineticObject.put("text", object.getText());
        kineticObject.put("fontFamily", object.getFont().getName());
        kineticObject.put("fontSize", 40);
        kineticObject.put("strokeWidth", object.getPenWidth());
        kineticObject.put("stroke", "");
        kineticObject.put("opacity", object.getPenColor().getAlpha() / 255.0);
        kineticObject.put("fill", TransformUtil.getColorString(object.getPenColor()));
        /*Map<String, Object> customAttributes = new HashMap<String, Object>();
        customAttributes.put("objectid", object.ID);
        System.out.println("on display object id:"+object.ID);
        kineticObject.put("custom", customAttributes);*/
        kineticObject.put("originY", "top");
        kineticObject.put("originX", "left");
        System.out.println("scaleX "+ object.getScaleX());
        kineticObject.put("scaleX", object.getScaleX());
        kineticObject.put("scaleY", object.getScaleY());
        return kineticObject;
	}

	@Override
	protected VisualObject decodeText(Map<String, Object> serialized) {
        double x = getDouble(serialized.get("left"));
        double y = getDouble(serialized.get("top"));
        double width=getDouble(serialized.get("width"));
        double height=getDouble(serialized.get("height"));
        String text = (String) serialized.get("text");
        String fontName = (String) serialized.get("fontFamily");
        int fontSize = new java.lang.Double(java.lang.Double.parseDouble((serialized.get("fontSize").toString())))
                .intValue();
        Font font = new Font(fontName, Font.PLAIN, fontSize);
        TextBox textBox;
        textBox = new TextBox(x, y, width, height, text);
        textBox.setFont(font);
        float opacity = getFloat(serialized.get("opacity"));
        int op = (int) (opacity*255);
        textBox.setPenColor(TransformUtil.parseHexString((String) serialized.get("fill"), op));   // fill is colour of text
        float strokewidth = getFloat(serialized.get("strokeWidth"));    //stroke width not required for text
        textBox.setPenWidth(strokewidth);
        textBox.setType(VisualObjectType.TEXT);
        float scaleX = getFloat(serialized.get("scaleX"));
        float scaleY = getFloat(serialized.get("scaleY"));
        textBox.setScaleX(scaleX);
        textBox.setScaleY(scaleY);
        System.out.println("textBox scaleX" + textBox.getScaleX());
        return textBox;
	}

	@Override
	protected Map<String, Object> encodePath(GeometricPath object) {
		logger.logp(Level.INFO, "KineticTransformer", "encodePath","");
        Map<String, Object> path = getCommonKineticObject(object);
        path.put("type", "path");
        path.put("originY", "center");
        path.put("originX", "center");
        path.put("strokeLineCap","round");
        path.put("strokeLineJoin", "round");
        path.put("fill", "");
        path.put("path", getPathString(object.getPathPoints()));
        return path;
	}

	@Override
	protected VisualObject decodePath(Map<String, Object> serialized) {
        GeometricPath path;
        if (serialized.containsKey("custom")) {
            Map<String, Object> customData = (Map<String, Object>) serialized.get("custom");
            int ID = Integer.parseInt(customData.get("objectid").toString());
            path = new GeometricPath(ID, 0);
        } else {
            path = new GeometricPath();
        }
        ArrayList al = (ArrayList) serialized.get("path");
        System.out.println(al.toString());
        ArrayList firstPoint = (ArrayList) al.get(0);
        if(!firstPoint.get(0).equals("M")){
        	throw new RuntimeException("Invalid path string: " + al);
        }
        Iterator itr = al.iterator();
        List<double[]> ret = new ArrayList<double[]>();
        while(itr.hasNext()){
        	//System.out.println(itr.next().getClass());
        	ArrayList tmp = (ArrayList) itr.next();
        	double[] first = new double[2];
        	double[] second = new double[2];
        	if(tmp.get(0).equals("Q")){
        		first[0] = (double) getDouble(tmp.get(1));
        		first[1] = (double) getDouble(tmp.get(2));
        		ret.add(first);
        		second[0] = (double) getDouble(tmp.get(3));
        		second[1] = (double) getDouble(tmp.get(4));
        		ret.add(second);
        	}
        	else if(tmp.get(0).equals("L")){
        		first[0] = (double) getDouble(tmp.get(1));
        		first[1] = (double) getDouble(tmp.get(2));
        		ret.add(first);
        	}
        	
        }
            
        List<double[]> points = ret;
        for (double[] next : points) {
            path.lineTo(next[0], next[1]);
        }
        path.setType(VisualObjectType.PATH);
        addCommonAttributes(path, serialized);
        return path;
	}

	@Override
	protected Map<String, Object> encodeLine(LineSegment object) {
        Map<String, Object> line = getCommonKineticObject(object);
        line.put("type", "Line");
        
        double[] points=new double[4];
        points[0]=object.startX;
        points[1]=object.startY;
        points[2]=object.endX;
        points[3]=object.endY;
        
        line.put("points", points);
        return line;
	}

	@Override
	protected VisualObject decodeLine(Map<String, Object> serialized) {
		logger.logp(Level.INFO, "KineticTransformer", "decodeLine", "points="+serialized.get("points").getClass());
		List<double[]> points=getLinePoints(serialized.get("points"));
        if (points.size() != 2) {
            throw new RuntimeException("Line string invalid: " + serialized.get("path"));
        }
        double startX = points.get(0)[0];
        double startY = points.get(0)[1];
        double endX = points.get(1)[0];
        double endY = points.get(1)[1];

        LineSegment line;
        if (serialized.containsKey("custom")) {
            Map<String, Object> customData = (Map<String, Object>) serialized.get("custom");
            int ID = Integer.parseInt(customData.get("objectid").toString());
            line = new LineSegment(ID, startX, startY, endX, endY);
        } else {
            line = new LineSegment(startX, startY, endX, endY);
        }
        line.setType(VisualObjectType.LINE);
        addCommonAttributes(line, serialized);
        return line;
	}
	
	@Override
	protected VisualObject decodePolygon(Map<String, Object> serialized) {
        Polygon polygon;
        if (serialized.containsKey("custom")) {
            Map<String, Object> customData = (Map<String, Object>) serialized.get("custom");
            int ID = Integer.parseInt(customData.get("objectid").toString());
            polygon = new Polygon(ID, 0);
        } else {
        	polygon = new Polygon(0);
        }
        ArrayList al = (ArrayList) serialized.get("points");
        double x = getDouble(serialized.get("left"));
        double y = getDouble(serialized.get("top"));
        Iterator itr = al.iterator();
        List<double[]> ret = new ArrayList<double[]>();
        double[] origin = new double[2];
        origin[0] = x;
        origin[1] = y;
        ret.add(origin);
        while(itr.hasNext()){
        	//System.out.println(itr.next().getClass());
        	LinkedHashMap<String, Object> pointMap = (LinkedHashMap<String, Object>) itr.next();
        	double[] point = new double[2];
        	point[0] = getDouble(pointMap.get("x"));
        	point[1] = getDouble(pointMap.get("y"));
        	ret.add(point);
        }
            
        List<double[]> points = ret;
        for (double[] next : points) {
        	polygon.lineTo(next[0], next[1]);
        }
        //to close the polygon
        //polygon.lineTo(points.get(0)[0], points.get(0)[1]);
        polygon.setType(VisualObjectType.POLYGON);
        addCommonAttributes(polygon, serialized);
        return polygon;
	}

	@Override
	protected Map<String, Object> encodePolygon(GeometricPath object) {
		logger.logp(Level.INFO, "KineticTransformer", "encodePolygon","");		
        Map<String, Object> polygon = getCommonKineticObject(object);
        polygon.put("type", "polygon");
        
        //remove last lineto which is used to close polygon
        List<Point2D> points = object.getPathPoints();
        Point2D origin = points.get(0);
        polygon.put("left", origin.getX());
        polygon.put("top", origin.getY());
        points.remove(0);
        /*ArrayList<HashMap<String, java.lang.Double>>  al = new ArrayList<>();
        Iterator<Point2D> itr = points.iterator();
        while(itr.hasNext()){
        	
        }*/
        //polygon.put("originX", "center");
        //polygon.put("originY", "center");
        		
        polygon.put("points", points);
        polygon.put("fill", "");
        
        return polygon;
	}	

	@Override
	protected VisualObject decodeArrow(Map<String, Object> serialized) {
        LineSegment arrow;
        
       
        double top = (double) getDouble(serialized.get("top"));
        double left = (double) getDouble(serialized.get("left"));
        double x1 = (double) getDouble(serialized.get("x1"));
        double y1 = (double) getDouble(serialized.get("y1"));
        //double x2 = (double) serialized.get("x2");
        //double y2 = (double) serialized.get("y2");
        arrow = new LineSegment(left, top, x1, y1);
        /*double[] start = {x1,y1};
        double[] end = {x2, y2};
        List<double[]> points = new ArrayList<double[]>();
        points.add(start);
        points.add(end);
        for (double[] next : points) {
        	arrow.lineTo(next[0], next[1]);
        }*/
        arrow.setType(VisualObjectType.LINE);
        addCommonAttributes(arrow, serialized);
        return arrow;		
	}

	@Override
	protected Map<String, Object> encodeArrow(GeometricPath object) {
        Map<String, Object> arrow = getCommonKineticObject(object);
        arrow.put("type", "arrow"); 
        arrow.put("stroke", TransformUtil.getColorString(object.getPenColor()));
        arrow.put("points",  getPathString(object.getPathPoints()));
        return arrow;
	}
	
    private static java.lang.Double getDouble(Object obj) {
        if (obj == null)
            return null;
        String doubleString = obj.toString();
        return java.lang.Double.parseDouble(doubleString);
    }

    private static Float getFloat(Object obj) {
        if (obj == null)
            return null;
        String doubleString = obj.toString();
        double value = java.lang.Double.parseDouble(doubleString);
        return (new java.lang.Double(value)).floatValue();
    }

    /**
     * Add attributes common to all kinetic objects
     * 
     * @param vo
     * @param data
     */
    private void addCommonAttributes(VisualObject vo, Map<String, Object> data) {
    	float opacity = getFloat(data.get("opacity"));
    	int op = (int) (opacity*255);
        Color color = TransformUtil.parseHexString((String) data.get("stroke"), op);
        vo.setPenColor(color);
        float width = getFloat(data.get("strokeWidth"));
        vo.setPenWidth(width);
        float angle=getFloat(data.get("angle"));
        vo.setRotationInDegrees(angle);
        float scaleX = getFloat(data.get("scaleX"));
        float scaleY = getFloat(data.get("scaleY"));
        vo.setScaleX(scaleX);
        vo.setScaleY(scaleY);
    }
    
    /**
     * Get a Kinetic object with attributes common to all shapes
     * @param obj
     * @return
     */
    private Map<String, Object> getCommonKineticObject(VisualObject obj) {
        Map<String, Object> ro = new HashMap<String, Object>();
        ro.put("opacity", obj.getPenColor().getAlpha() / 255.0);
        ro.put("stroke", TransformUtil.getColorString(obj.getPenColor()));
        ro.put("strokeWidth", obj.getPenWidth());
       /* Map<String, Object> customAttributes = new HashMap<String, Object>();
        customAttributes.put("objectid", obj.ID);
        ro.put("custom", customAttributes);*/
        ro.put("scaleX",obj.getScaleX());
        ro.put("scaleY",obj.getScaleY());
        ro.put("angle", obj.getRotationInDegrees());
        return ro;
    }
    
    /**
     * Get list of path points from a path string
     * 
     * @param pathString
     * @return
     */
    private static List<double[]> getPathPoints(String pathString) {
        if (!pathString.startsWith("M"))
            throw new RuntimeException("Invalid path string: " + pathString);
        List<double[]> ret = new ArrayList<double[]>();
        String[] pointStrings = pathString.substring(1).split("L");
        for (String string : pointStrings) {
            String[] split = string.split(",");
            if (split.length != 2)
                throw new RuntimeException("Invalid coordinate: " + string + " in path string: " + pathString);
            double[] next = new double[2];
            next[0] = getDouble(split[0]);
            next[1] = getDouble(split[1]);
            ret.add(next);
        }
        return ret;
    }

    /**
     * Get kinetic path string from list of points
     * 
     * @param pathPoints
     * @return
     */
    private String getPathString(List<Point2D> pathPoints) {
        StringBuilder builder = new StringBuilder("[");
        
        Point2D start = pathPoints.get(0);
        builder.append("[\"M\"");
        builder.append(",");
        builder.append(start.getX());
        builder.append(",");
        builder.append(start.getY());
        builder.append("]");
        
        System.out.println(builder.toString());
        for (int i = 0; i < pathPoints.size() - 1; ++i) {
        	
            Point2D point_i = pathPoints.get(i);
            Point2D point_f = pathPoints.get(i+1);
            
            builder.append(",[\"Q\",");
            builder.append(point_i.getX());
            builder.append(",");
            builder.append(point_i.getY());
            builder.append(",");
            builder.append(point_f.getX());
            builder.append(",");
            builder.append(point_f.getY());
            builder.append("]");
            
        }
        Point2D lastPoint = pathPoints.get(pathPoints.size() - 1);
        builder.append(",[\"L\"");
        builder.append(",");
        builder.append(lastPoint.getX());
        builder.append(",");
        builder.append(lastPoint.getY());
        builder.append("]]");
        System.out.println(builder);
        return builder.toString();
    }
    
    /**
     * Get point of a line
     * @param points
     * @return
     */
    private List<double[]> getLinePoints(Object points){
    	 List<double[]> ret = new ArrayList<double[]>();
    	 for( int i=0;i<((List<Object>) points).size();i=i+2){
    		 double[] next = new double[2];
    		 next[0]=java.lang.Double.valueOf((String)((List<Object>)points).get(i));
    		 next[1]=java.lang.Double.valueOf((String)((List<Object>)points).get(i+1));
    		 ret.add(next);
    	 }
    	  	 
    	return ret;
    }

}
