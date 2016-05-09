package com.graphoscope.util;

import java.io.File;

import com.strandgenomics.imaging.icore.Constants;

public class MultiThreadTiler {
	int readers;
	int cores;
	int threads;
	public MultiThreadTiler(int readers){
		cores = Runtime.getRuntime().availableProcessors();
		readers = Constants.getBufferedReaderStackSize();
		readers = 8;
		threads = Math.min(readers, 2*cores);
	}
	public void executeTiling(){
		RecordHolder r = new RecordHolder("/home/ravikiran/curie_Data/tumour/Normal_008.tif");
		//RecordHolder r = new RecordHolder("/home/ravikiran/svsfiles/1.svs");
		//RecordHolder r = new RecordHolder("/home/ravikiran/FMG_344.tiff");
		int height = r.getHeight();
		int width = r.getWidth();
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
		TilerThread[] t_s = new TilerThread[threads];
		int[] channelNos = {0,1,2};
		int size = Math.max(width,height);
		
		if(width > height){
			int div = size/threads;
			div = div - div%1024;
			System.out.println("div " + div );
			for(int i = 0; i < threads; i++){
				if(i != threads - 1)
					t_s[i] = new TilerThread("Thread " + i,0,t,i*div, 0, div*(i+1), height,channelNos); 
				else
					t_s[i] = new TilerThread("Thread " + i,0,t,i*div, 0, width, height,channelNos);
			}
		}
		else{
			int div = size/threads;
			div = div - div%1024;
			System.out.println("div " + div );
			for(int i = 0; i < threads; i++){
				if(i != threads - 1)
					t_s[i] = new TilerThread("Thread " + i,0,t,0, i*div, width, (i+1)*div,channelNos); 
				else
					t_s[i] = new TilerThread("Thread " + i,0,t,0, i*div, width, height,channelNos);
			}
		}
		for(int i = 0; i < threads; i++){
			t_s[i].start();
		}
		
	}
}
