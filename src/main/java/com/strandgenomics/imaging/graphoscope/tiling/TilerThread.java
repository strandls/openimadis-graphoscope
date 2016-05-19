package com.strandgenomics.imaging.graphoscope.tiling;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TilerThread extends Thread{
	private long start;
	Tiling t;
	private int start_x, start_y, end_x, end_y;
	private int[] channelNos;
	public TilerThread(){
		
	}
	public TilerThread(String label,long start,Tiling t,int start_x, int start_y, int end_x, int end_y, int[] channelNos){
		super("thread '" + label + "'");
		this.start = start;
		this.t = t;
		this.start_x = start_x;
		this.start_y = start_y;
		this.end_x = end_x;
		this.end_y = end_y;
		this.channelNos = channelNos;
	}
	public void run () {
		try {
			t.doTiling(start_x, start_y, end_x, end_y);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis() - start;
		System.out.println("end" + getName() + " " + end );
	}
	
	

}

