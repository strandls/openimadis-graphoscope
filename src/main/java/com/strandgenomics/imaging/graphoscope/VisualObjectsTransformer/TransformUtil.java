/*
 * TransformUtil.java
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

/**
 * Utilities used while transforming from server side to web client objects and
 * vice versa.
 * 
 * @author santhosh
 * 
 */
public class TransformUtil {

    /**
     * Get hex string equivalent of {@link Color}
     * 
     * @param colour
     * @return hex string
     */
    public static String getColorString(Color colour) {
        return "#" + getHexString(colour.getRed()) + getHexString(colour.getGreen()) + getHexString(colour.getBlue());
    }

    /**
     * Get two character hex string equivalent of the given colour
     * 
     * @param colour
     * @return
     */
    private static String getHexString(int colour) {
        String hexString = Integer.toHexString(colour);
        if (hexString.length() == 1)
            return "0" + hexString;
        return hexString;
    }

    /**
     * Parse the given hex string to get {@link Color} instance
     * 
     * @param hex
     *            hex string. should be of the for "#??????"
     * @param opacity
     *            opacity to use. should be between 0 and 1
     * @return color instance
     */
    public static Color parseHexString(String hex, int opacity) {
        if (!hex.startsWith("#") || hex.length() != 7)
            throw new RuntimeException("Not a valid hexstring: " + hex);
        String redString = hex.substring(1, 3);
        String greenString = hex.substring(3, 5);
        String blueString = hex.substring(5, 7);
        int red = Integer.parseInt(redString, 16);
        int green = Integer.parseInt(greenString, 16);
        int blue = Integer.parseInt(blueString, 16);
        int alpha= opacity;
        return new Color(red, green, blue, alpha);
    }

    /**
     * Parse the given hex string to get {@link Color} instance
     * 
     * @param hex
     *            hex string. should be of the for "#??????"
     * @return color instance
     */
    public static Color parseHexString(String hex) {
        if (!hex.startsWith("#") || hex.length() != 7)
            throw new RuntimeException("Not a valid hexstring: " + hex);
        String redString = hex.substring(1, 3);
        String greenString = hex.substring(3, 5);
        String blueString = hex.substring(5, 7);
        int red = Integer.parseInt(redString, 16);
        int green = Integer.parseInt(greenString, 16);
        int blue = Integer.parseInt(blueString, 16);
        return new Color(red, green, blue);
    }
}
