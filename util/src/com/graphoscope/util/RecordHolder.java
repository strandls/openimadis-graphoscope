package com.graphoscope.util;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.imageio.ImageIO;

import com.strandgenomics.imaging.iclient.local.DefaultImportRequest;
import com.strandgenomics.imaging.iclient.local.RawExperiment;
import com.strandgenomics.imaging.iclient.local.RawExperimentFactory;
import com.strandgenomics.imaging.icore.IExperiment;
import com.strandgenomics.imaging.icore.IPixelDataOverlay;
import com.strandgenomics.imaging.icore.IRecord;
import com.strandgenomics.imaging.icore.IRecordObserver;


public class RecordHolder implements IRecordObserver {
	IRecord record  = null;
	public  RecordHolder(String file) {
		// TODO Auto-generated method stub
		File f = new File(file);
		RawExperiment rf = RawExperimentFactory.createExperiment(f, false);
		try {
			rf.extractRecords(this, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int record_count = rf.getRecords().size();
		System.out.println("length" + rf.getRecords().size());
		
		if( record_count > 0){
			int height = 0, width = 0;
			int index = 0;
			//IRecord[] records = (IRecord[]) rf.getRecords().toArray();
			for(int i = 0; i < record_count; i++){
				IRecord record = (IRecord) rf.getRecords().toArray()[i];
				if(height < record.getImageHeight() && width < record.getImageWidth()){
					height = record.getImageHeight();
					width = record.getImageWidth();
					index = i;
				}
			}
			record = (IRecord) rf.getRecords().toArray()[index];
		}
		else{
			record = (IRecord) rf.getRecords().toArray()[0];
		}
		
	}
	public BufferedImage getImage(int x,int y, int w, int h,int[] channelNos){
		
		BufferedImage img = null;
		try {
			IPixelDataOverlay ip = record.getOverlayedPixelData(0, 0, 0, channelNos);
			
			Rectangle roi = new Rectangle(x,y,w,h);
			img = ip.getImage(false, false, true, roi);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return img;
	}
	public int getHeight(){
		return record.getImageHeight();
	}
	public int getWidth(){
		return record.getImageWidth();
	}
	public IRecord getRecord(){
		return record;
	}

	@Override
	public boolean isStopped() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void recordAdded(IRecord record) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void foundDuplicate(IExperiment experiment) {
		// TODO Auto-generated method stub
		
	}

}
