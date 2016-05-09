package com.graphoscope.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.strandgenomics.imaging.icore.VisualContrast;

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
			t.doTiling(start_x, start_y, end_x, end_y,channelNos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis() - start;
		System.out.println("end" + getName() + " " + end );
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//RecordHolder r = new RecordHolder("/home/ravikiran/svsfiles/1.svs");
		
		//RecordHolder r = new RecordHolder("/home/ravikiran/FMG_344.tiff");
		RecordHolder r = new RecordHolder("/home/ravikiran/curie_Data/tumour/Normal_008.tif");
		String img_format = "png";
		String root = "/home/ravikiran/TC";
		int levels = (int) (Math.log(Math.max(r.getHeight(), r.getWidth()))/Math.log(2)) + 1;
		for(int i = 0; i <= levels; i++){
			File subLevel = new File(root,String.valueOf(i));
			if(!subLevel.exists()){
				subLevel.mkdir();
			}
		}
		Tiling t = new Tiling(root,r, levels, img_format);
		long start = System.currentTimeMillis();
		/*ThreadExample t1 = new ThreadExample("A", start, t,0, 0, 8192, 4096);
		ThreadExample t2 = new ThreadExample("B", start, t,8192, 0, 8192+8192, 4096);
		ThreadExample t3 = new ThreadExample("C", start, t, 8192+8192,0, r.getWidth(), 4096);
		ThreadExample t4 = new ThreadExample("D", start, t,0, 4096, 8192, r.getHeight());
		ThreadExample t5 = new ThreadExample("E", start, t,8192, 4096, 8192+8192, r.getHeight());
		ThreadExample t6 = new ThreadExample("F", start, t,8192+8192, 4096, r.getWidth(), r.getHeight());*/
		int size = 1024*27;
		System.out.println("" + r.getRecord().getChannelCount());
		System.out.println(""+ r.getHeight() + "   " + r.getWidth());
		int[] channelNos = {0,1,2};
		for(int i = 0 ; i < channelNos.length; i++)
			r.getRecord().setCustomContrast(false, channelNos[i], new VisualContrast(0, 255));  
		TilerThread t1 = new TilerThread("A", start, t,0, 0, 97792, size*1,channelNos);
		TilerThread t2 = new TilerThread("B", start, t,0, size*1, 97792, size*2,channelNos);
		TilerThread t3 = new TilerThread("C", start, t,0, size*2, 97792, size*3,channelNos);
		TilerThread t4 = new TilerThread("D", start, t,0, size*3, 97792, size*4,channelNos );
		TilerThread t5 = new TilerThread("E", start, t,0,size*4, 97792, size*5,channelNos);
		TilerThread t6 = new TilerThread("F", start, t,0,size*5 , 97792,size*6 ,channelNos);
		TilerThread t7 = new TilerThread("G", start, t,0 ,size*6, 97792, size*7,channelNos);
		TilerThread t8 = new TilerThread("H", start, t,0 , size*7, 97792,r.getHeight(),channelNos);
		/*ThreadExample t9 = new ThreadExample("I", start, t,0 ,size*8,97792,size*9);
		ThreadExample t10 = new ThreadExample("J", start, t,0,size*9, 97792, r.getHeight());*/
//		ThreadExample t1 = new ThreadExample("A", start, t,0, 0, size, 19654,channelNos);
//		ThreadExample t2 = new ThreadExample("B", start, t,size*1,0, size*2, 19654,channelNos);
//		ThreadExample t3 = new ThreadExample("C", start, t,size*2,0, size*3, 19654,channelNos);
//		ThreadExample t4 = new ThreadExample("D", start, t,size*3,0, size*4, 19654,channelNos);
//		ThreadExample t5 = new ThreadExample("E", start, t,size*4,0, size*5, 19654,channelNos);
//		ThreadExample t6 = new ThreadExample("F", start, t,size*5,0, size*6, 19654,channelNos);
//		ThreadExample t7 = new ThreadExample("G", start, t,size*6,0, size*7, 19654,channelNos);
//		ThreadExample t8 = new ThreadExample("H", start, t,size*7,0, 85680, 19654,channelNos);
		t1.start();
		t2.start();
		/*t3.start();
		t4.start();
		t5.start();
		t6.start();
		t7.start();
		t8.start();*/
//		t9.start();
//		t10.start();
	}
	public static int getThreadCount(int readers, int cores){
		int threads = cores;
		if(cores > readers){
			threads = readers; 
		}
		return 0;
	}

}
