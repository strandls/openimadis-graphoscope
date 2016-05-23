package com.strandgenomics.imaging.graphoscope.tiling;


import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.strandgenomics.imaging.iclient.ImageSpaceSystem;
import com.strandgenomics.imaging.iclient.Record;
import com.strandgenomics.imaging.icore.util.Util;
import com.strandgenomics.imaging.tileviewer.Helper;

public class SimpleTiler {
	
	private final int DZI_TILE_SIZE = 256;
	private  String img_format = "png";
	private  File root;
	
	private long recordId;
	private ImageSpaceSystem ispace;
	private RecordParameters recordParams;
	public SimpleTiler(File storage,long recordId, ImageSpaceSystem iSpace, RecordParameters params){
		this.root = storage;
		this.recordId = recordId;
		this.img_format = "png";
		this.ispace = iSpace;
		this.recordParams = params;
	}
	
	public  void doTiling(int start_x,int start_y,int end_x,int end_y) throws IOException {
		/*Stitcher s = new Stitcher("/home/ravikiran/TC",3840 , 2160, 12,"jpeg");
		try {
			s.createAllLevels();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		System.out.println("doTiling enter simple tiling.........................................");
		System.out.println(recordParams.toString());
		Record record = ispace.findRecordForGUID(recordId);
		File record_dir = new File(root, "" + recordId);
		if(!record_dir.exists()){
			record_dir.mkdir();
		}
		int height = (int) record.getImageHeight();
		int width = (int) record.getImageWidth();
		int levels = (int) (Math.log(Math.max(height, width))/Math.log(2)) + 1;
		for(int i = 0; i <= levels; i++){
			File subLevel = new File(record_dir,String.valueOf(i));
			if(!subLevel.exists()){
				subLevel.mkdir();
			}
		}
		File level_dir = new File(record_dir.getAbsolutePath() + File.separator + String.valueOf(levels));
		if(!level_dir.exists()){
			level_dir.mkdir();
		}
		int count = 0;
		System.out.println("" + record.getImageWidth());
		
		//int[] channelNos = {0,1,2}; 
		int frameNumber = recordParams.getFrameNumber();
		int sliceNumber = recordParams.getSliceNumber();
		int channelCount = recordParams.getChannelCount();

		boolean isGrayScale = recordParams.isGrayScale();
		boolean isZStacked = recordParams.isZStacked();
		int[] channelNos = new int[channelCount];
		for(int i = 0; i < channelCount; i++){
			channelNos[i] = i;
		}


		BufferedImage chunk = null;
		
		int rows = height/DZI_TILE_SIZE;
		int columns = width/DZI_TILE_SIZE;
		int x_end = width%DZI_TILE_SIZE;
		int y_end = height%DZI_TILE_SIZE;
		System.out.println(""+rows + " " + columns);
		for(int j = 0; j < columns; j++){
			for(int i =0; i < rows; i++){
				chunk = ispace.getOverlayedImage(recordId, sliceNumber, frameNumber, 0, channelNos, isZStacked, false, !isGrayScale, DZI_TILE_SIZE*j, DZI_TILE_SIZE*i, DZI_TILE_SIZE, DZI_TILE_SIZE);
				try {
					ImageIO.write(chunk, img_format, new File(level_dir.getAbsolutePath() + File.separator + String.valueOf(j)+ "_" + String.valueOf(i) + "." + img_format));
					count++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(y_end > 0){
				chunk = ispace.getOverlayedImage(recordId, sliceNumber, frameNumber, 0, channelNos, isZStacked, false, !isGrayScale, DZI_TILE_SIZE*j, height - y_end, DZI_TILE_SIZE, y_end);
				//chunk = createResizedCopy(chunk, requiredWidth/columns, requiredHeight/rows, true);
				try {
					ImageIO.write(chunk, img_format, new File(level_dir.getAbsolutePath() + File.separator + String.valueOf(j)+ "_" + String.valueOf(rows) + "." + img_format));
					count++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		if(x_end > 0){
			for(int i =0; i < rows; i++){
				chunk = ispace.getOverlayedImage(recordId, sliceNumber, frameNumber, 0, channelNos, isZStacked, false, !isGrayScale, DZI_TILE_SIZE*columns, DZI_TILE_SIZE*i, x_end, DZI_TILE_SIZE);
				//chunk = createResizedCopy(chunk, requiredWidth/columns, requiredHeight/rows, true);
				try {
					ImageIO.write(chunk, img_format, new File(level_dir.getAbsolutePath() + File.separator + String.valueOf(columns)+ "_" + String.valueOf(i) + "." + img_format));
					count++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(y_end > 0){
				chunk = ispace.getOverlayedImage(recordId, sliceNumber, frameNumber, 0, channelNos, isZStacked, false, !isGrayScale, DZI_TILE_SIZE*columns, height-y_end,x_end, y_end);
				//chunk = createResizedCopy(chunk, requiredWidth/columns, requiredHeight/rows, true);
				try {
					ImageIO.write(chunk, img_format, new File(level_dir.getAbsolutePath() + File.separator + String.valueOf(columns)+ "_" + String.valueOf(rows) + "." + img_format));
					count++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		Stitcher s = new Stitcher(record_dir.getAbsolutePath(),record.getImageWidth(), record.getImageHeight(), levels,img_format);
		try {
			s.createAllLevels();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("count" + count);
		System.out.println("leaving/////////////////////////////////");
	}
	

}

