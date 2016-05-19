package com.strandgenomics.imaging.graphoscope.tiling;


import java.io.File;
import java.io.IOException;

import com.strandgenomics.imaging.iclient.ImageSpaceSystem;
import com.strandgenomics.imaging.iclient.Record;
import com.strandgenomics.imaging.icore.Constants;
import com.strandgenomics.imaging.graphoscope.tiling.Stitcher;

public class MultiThreadTiler {
	int readers;
	int cores;
	int threads;
	private long recordId;
	private ImageSpaceSystem ispace;
	private File storageroot;
	private RecordParameters recordParams;
	public MultiThreadTiler(long recordId,ImageSpaceSystem iSpace,File storageroot,RecordParameters recordParams){
		cores = Runtime.getRuntime().availableProcessors();
		readers = Constants.getBufferedReaderStackSize();
		readers = 4;
		threads = Math.min(readers, 2*cores);
		this.recordId = recordId;
		this.ispace = iSpace;
		this.storageroot = storageroot;
		this.recordParams = recordParams;
	}
	public void executeTiling(){
		Record record = ispace.findRecordForGUID(recordId);
		int height = record.getImageHeight();
		int width = record.getImageWidth();
		String img_format = "png";
		
		File record_dir = new File(storageroot, "" + recordId);
		if(!record_dir.exists()){
			record_dir.mkdir();
		}
		String root = record_dir.getAbsolutePath();
		int levels = (int) (Math.log(Math.max(height, width))/Math.log(2)) + 1;
		for(int i = 0; i <= levels; i++){
			File subLevel = new File(root,String.valueOf(i));
			if(!subLevel.exists()){
				subLevel.mkdir();
			}
		}
		Tiling t = new Tiling(root,recordId, levels, img_format,ispace, recordParams);
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
		for(int i = 0; i < threads; i++){
			try {
				t_s[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
		}
		//create all the lower levels from highest level
		/*Stitcher s = new Stitcher(root,record.getImageWidth(), record.getImageHeight(), levels,img_format);
		try {
			s.createAllLevels();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
