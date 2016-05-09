package com.graphoscope.util;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//RecordHolder r = new RecordHolder("/home/ravikiran/curie_Data/tumour/Normal_005.tif");
		RecordHolder r = new RecordHolder("/home/ravikiran/svsfiles/1.svs");
		//RecordHolder r = new RecordHolder("/home/ravikiran/4.jpg");
		String img_format = "png";
		String root = "/home/ravikiran/TC";
		int levels = (int) (Math.log(Math.max(r.getHeight(), r.getWidth()))/Math.log(2)) + 1;
		for(int i = 0; i <= levels; i++){
			File subLevel = new File(root,String.valueOf(i));
			if(!subLevel.exists()){
				subLevel.mkdir();
			}
		}
		/*long start = System.currentTimeMillis();
		Tiling t = new Tiling(root,r, levels, img_format);
		int[] channelNos = {0};
		try {
			t.doTiling(0,0,3840,2160,channelNos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("time"+ (System.currentTimeMillis() - start));*/
		Stitcher s = new Stitcher(root,r.getWidth(), r.getHeight(), levels,img_format);
		try {
			s.createAllLevels();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
