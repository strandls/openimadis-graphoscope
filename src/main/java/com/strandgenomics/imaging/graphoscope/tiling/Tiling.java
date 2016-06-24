package com.strandgenomics.imaging.graphoscope.tiling;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.strandgenomics.imaging.iclient.ImageSpaceSystem;
import com.strandgenomics.imaging.iclient.Record;

public class Tiling {
	
	private final int MAX_TILE_SIZE = 1024;
	private final int DZI_TILE_SIZE = 256;
	private  String img_format = "png";
	private  File root;
	private int level;
	
	private long recordId;
	private ImageSpaceSystem ispace;
	private RecordParameters recordParams;
	public Tiling(String path, long recordId,int level, String format, ImageSpaceSystem iSpace, RecordParameters params){
		this.root = new File(path);
		this.recordId = recordId;
		this.level = level;
		this.img_format = format;
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
		System.out.println("doTiling enter.........................................");
		System.out.println(recordParams.toString());
		Record record = ispace.findRecordForGUID(recordId);
		File level_dir = new File(root.getAbsolutePath() + File.separator + String.valueOf(level));
		if(!level_dir.exists()){
			level_dir.mkdir();
		}
		int count = 0;
		System.out.println("" + record.getImageWidth());
		int chunk_height = end_y -start_y;
		int chunk_width = end_x - start_x;
		int record_height = record.getImageHeight() - record.getImageHeight()%MAX_TILE_SIZE;
		int record_width = record.getImageWidth() - record.getImageWidth()%MAX_TILE_SIZE;
		int record_height_extra =  record.getImageHeight()%MAX_TILE_SIZE;
		int record_width_extra =  record.getImageWidth()%MAX_TILE_SIZE;
		int height = chunk_height - chunk_height%MAX_TILE_SIZE;
		int width = chunk_width - chunk_width%MAX_TILE_SIZE;
		int height_extra =  chunk_height %MAX_TILE_SIZE;
		int width_extra =  chunk_width%MAX_TILE_SIZE;
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
		int factor = MAX_TILE_SIZE/DZI_TILE_SIZE;
		for(int x=start_x; x < end_x - width_extra ;x+=MAX_TILE_SIZE)
		{
			for(int y=start_y;y<end_y - height_extra;y+=MAX_TILE_SIZE)
			{
				int start_X = x;
				int start_Y = y;
				//BufferedImage bim = record.getImageImage(x, y, MAX_TILE_SIZE, MAX_TILE_SIZE, channelNos);
				BufferedImage bim = ispace.getOverlayedImage(recordId, sliceNumber, frameNumber, 0, channelNos, isZStacked, false, !isGrayScale, x, y, MAX_TILE_SIZE, MAX_TILE_SIZE);
				for(int i = 0; i < factor; i++){
					for(int j = 0; j < factor; j++){
						BufferedImage tile = bim.getSubimage(i*DZI_TILE_SIZE, j*DZI_TILE_SIZE, DZI_TILE_SIZE, DZI_TILE_SIZE);
						int tile_i = start_X/DZI_TILE_SIZE + i;
						int tile_j = start_Y/DZI_TILE_SIZE + j;
						System.out.println("tile pos" + tile_i + "_"+ tile_j);
						count++;
						ImageIO.write(tile, img_format, new File(level_dir.getAbsolutePath() + File.separator + String.valueOf(tile_i)+ "_" + String.valueOf(tile_j) + "." + img_format));
					}
				}
				bim.flush();
				System.out.println("done");
			}
			
		} 
		if(width_extra > 0){
			System.out.println("enter width extra");
			int x_new = record.getImageWidth() - 1024;
			int y_new = record.getImageHeight() - 1024;
			for(int y=start_y; y< end_y - height_extra; y+=MAX_TILE_SIZE)
			{
				int start_Y = y;
				//BufferedImage bim = record.getImage(x_new, y, MAX_TILE_SIZE, MAX_TILE_SIZE, channelNos);
				BufferedImage bim = ispace.getOverlayedImage(recordId, sliceNumber, frameNumber, 0, channelNos, isZStacked, false, !isGrayScale, x_new, y, MAX_TILE_SIZE, MAX_TILE_SIZE);
				BufferedImage c_bim = bim.getSubimage(width-(chunk_width - MAX_TILE_SIZE), 0 , width_extra, MAX_TILE_SIZE);
				int x_factor = width_extra/DZI_TILE_SIZE;
				for(int i = 0; i < x_factor; i++){
					for(int j = 0; j < factor; j++){
						BufferedImage tile = c_bim.getSubimage(i*DZI_TILE_SIZE, j*DZI_TILE_SIZE, DZI_TILE_SIZE, DZI_TILE_SIZE);
						int tile_i = record_width/DZI_TILE_SIZE + i;
						int tile_j = y/DZI_TILE_SIZE + j;
						System.out.println(tile_i + "_"+ tile_j);
						count++;
						ImageIO.write(tile, img_format, new File(level_dir.getAbsolutePath() + File.separator +  String.valueOf(tile_i)+ "_" + String.valueOf(tile_j) + "." + img_format));
					}
				}
				if(width_extra%DZI_TILE_SIZE != 0){
					for(int j = 0; j < factor; j++){
						BufferedImage tile = c_bim.getSubimage(x_factor*DZI_TILE_SIZE, j*DZI_TILE_SIZE, width_extra%DZI_TILE_SIZE, DZI_TILE_SIZE);
						int tile_i = record_width/DZI_TILE_SIZE + x_factor;
						int tile_j = y/DZI_TILE_SIZE + j;
						System.out.println(tile_i + "_"+ tile_j);
						count++;
						ImageIO.write(tile, img_format, new File(level_dir.getAbsolutePath() + File.separator +  String.valueOf(tile_i)+ "_" + String.valueOf(tile_j) + "." + img_format));
					}
				}
				System.out.println("done");
				bim.flush();
			}
		}
		if(height_extra > 0){
			System.out.println("enter height extra");
			int y_new  = record.getImageHeight() - 1024;
			for(int x=start_x; x<end_x - width_extra; x+=MAX_TILE_SIZE)
			{
				int start_X = x;
				//BufferedImage bim = record.getImage(x, y_new, MAX_TILE_SIZE, MAX_TILE_SIZE, channelNos);
				BufferedImage bim = ispace.getOverlayedImage(recordId, sliceNumber, frameNumber, 0, channelNos, isZStacked, false, !isGrayScale, x, y_new, MAX_TILE_SIZE, MAX_TILE_SIZE);
				BufferedImage c_bim = bim.getSubimage(0, height - (chunk_height - MAX_TILE_SIZE) , MAX_TILE_SIZE, height_extra);
				int y_factor = height_extra/DZI_TILE_SIZE;
				for(int i = 0; i < factor; i++){
					for(int j = 0; j < y_factor; j++){
						BufferedImage tile = c_bim.getSubimage(i*DZI_TILE_SIZE, j*DZI_TILE_SIZE, DZI_TILE_SIZE, DZI_TILE_SIZE);
						int tile_i = x/DZI_TILE_SIZE + i;
						int tile_j = record_height/DZI_TILE_SIZE + j;
						System.out.println(tile_i + "_"+ tile_j);
						count++;
						ImageIO.write(tile, img_format, new File(level_dir.getAbsolutePath()  + File.separator +  String.valueOf(tile_i)+ "_" + String.valueOf(tile_j) + "." + img_format));
					}
				}
				if(height_extra%DZI_TILE_SIZE != 0){
					for(int j = 0; j < factor; j++){
						BufferedImage tile = c_bim.getSubimage(j*DZI_TILE_SIZE, y_factor*DZI_TILE_SIZE, DZI_TILE_SIZE, height_extra%DZI_TILE_SIZE);
						int tile_i = x/DZI_TILE_SIZE + j;
						int tile_j = record_height/DZI_TILE_SIZE + y_factor;
						System.out.println(tile_i + "_"+ tile_j);
						count++;
						ImageIO.write(tile, img_format, new File(level_dir.getAbsolutePath()  + File.separator +  String.valueOf(tile_i)+ "_" + String.valueOf(tile_j) + "." + img_format));
					}
				}
				System.out.println("done");
				bim.flush();
			}
		}
		if(width_extra > 0 && height_extra > 0){
			int x_new = record.getImageWidth() - 1024;
			int y_new = record.getImageHeight() - 1024;
			//BufferedImage bim = record.getImage(x_new, y_new, MAX_TILE_SIZE, MAX_TILE_SIZE, channelNos);
			BufferedImage bim = ispace.getOverlayedImage(recordId, sliceNumber, frameNumber, 0, channelNos, isZStacked, false, !isGrayScale, x_new, y_new, MAX_TILE_SIZE, MAX_TILE_SIZE);
			BufferedImage c_bim = bim.getSubimage(width - (chunk_width - 1024), height - (chunk_height - 1024) , width_extra, height_extra);
			int n_x = width_extra/DZI_TILE_SIZE;
			int n_y = height_extra/DZI_TILE_SIZE;
			for(int i=0; i < n_x; i++){
				for(int j=0; j< n_y; j++){
					BufferedImage tile = c_bim.getSubimage(i*DZI_TILE_SIZE, j*DZI_TILE_SIZE , DZI_TILE_SIZE, DZI_TILE_SIZE);
					int tile_i = record_width/DZI_TILE_SIZE + i;
					int tile_j = record_height/DZI_TILE_SIZE + j;
					System.out.println(tile_i + "_"+ tile_j);
					count++;
					ImageIO.write(tile, img_format, new File(level_dir.getAbsolutePath() + File.separator + String.valueOf(tile_i)+ "_" + String.valueOf(tile_j) + "." + img_format));
				}
			}
			if(width_extra%DZI_TILE_SIZE != 0){
				for(int i = 0; i < n_y; i++){
					BufferedImage tile = c_bim.getSubimage(width_extra - width_extra%DZI_TILE_SIZE, i*DZI_TILE_SIZE, width_extra%DZI_TILE_SIZE, DZI_TILE_SIZE);
					int tile_i = record_width/DZI_TILE_SIZE + width_extra/DZI_TILE_SIZE;
					int tile_j = record_height/DZI_TILE_SIZE + i;
					System.out.println(tile_i + "_"+ tile_j);
					count++;
					ImageIO.write(tile, img_format, new File(level_dir.getAbsolutePath() + File.separator + String.valueOf(tile_i)+ "_" + String.valueOf(tile_j) + "." + img_format));
				}
			}
			if(height_extra%DZI_TILE_SIZE != 0){
				for(int i = 0; i < n_x; i++){
					BufferedImage tile = c_bim.getSubimage(i*DZI_TILE_SIZE, height_extra - height_extra%DZI_TILE_SIZE  , DZI_TILE_SIZE, height_extra%DZI_TILE_SIZE);
					int tile_i = record_width/DZI_TILE_SIZE + i;
					int tile_j = record_height/DZI_TILE_SIZE + height_extra/DZI_TILE_SIZE;
					System.out.println(tile_i + "_"+ tile_j);
					count++;
					ImageIO.write(tile, img_format, new File(level_dir.getAbsolutePath() + File.separator +  String.valueOf(tile_i)+ "_" + String.valueOf(tile_j) + "." + img_format));
				}
			}
			if(width_extra%DZI_TILE_SIZE != 0 && height_extra%DZI_TILE_SIZE != 0){
				BufferedImage tile = c_bim.getSubimage(width_extra - width_extra%DZI_TILE_SIZE, height_extra - height_extra%DZI_TILE_SIZE , width_extra%DZI_TILE_SIZE, height_extra%DZI_TILE_SIZE);
				int tile_i = record_width/DZI_TILE_SIZE + width_extra/DZI_TILE_SIZE;
				int tile_j = record_height/DZI_TILE_SIZE + height_extra/DZI_TILE_SIZE;
				System.out.println(tile_i + "_"+ tile_j);
				count++;
				ImageIO.write(tile, img_format, new File(level_dir.getAbsolutePath() + File.separator + String.valueOf(tile_i)+ "_" + String.valueOf(tile_j) + "." + img_format));
			}
		}
		System.out.println("count" + count);
		System.out.println("leaving/////////////////////////////////");
	}
	

}

