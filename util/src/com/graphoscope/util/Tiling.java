package com.graphoscope.util;


import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Tiling {
	
	private final int MAX_TILE_SIZE = 1024;
	private final int DZI_TILE_SIZE = 256;
	private  String img_format = "png";
	private  File root;
	private int level;
	
	private RecordHolder record;
	public Tiling(String path, RecordHolder record,int level, String format){
		this.root = new File(path);
		this.record = record;
		this.level = level;
		this.img_format = format;
	}
	
	public  void doTiling(int start_x,int start_y,int end_x,int end_y, int[] channelNos) throws IOException {
		/*Stitcher s = new Stitcher("/home/ravikiran/TC",3840 , 2160, 12,"jpeg");
		try {
			s.createAllLevels();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		File level_dir = new File(root.getAbsolutePath() + File.separator + String.valueOf(level));
		if(!level_dir.exists()){
			level_dir.mkdir();
		}
		int count = 0;
		System.out.println("" + record.getWidth());
		int chunk_height = end_y -start_y;
		int chunk_width = end_x - start_x;
		int record_height = record.getHeight() - record.getHeight()%MAX_TILE_SIZE;
		int record_width = record.getWidth() - record.getWidth()%MAX_TILE_SIZE;
		int record_height_extra =  record.getHeight()%MAX_TILE_SIZE;
		int record_width_extra =  record.getWidth()%MAX_TILE_SIZE;
		int height = chunk_height - chunk_height%MAX_TILE_SIZE;
		int width = chunk_width - chunk_width%MAX_TILE_SIZE;
		int height_extra =  chunk_height %MAX_TILE_SIZE;
		int width_extra =  chunk_width%MAX_TILE_SIZE;
		//int[] channelNos = {0,1,2}; 
		int factor = MAX_TILE_SIZE/DZI_TILE_SIZE;
		for(int x=start_x; x < end_x - width_extra ;x+=MAX_TILE_SIZE)
		{
			for(int y=start_y;y<end_y - height_extra;y+=MAX_TILE_SIZE)
			{
				int start_X = x;
				int start_Y = y;
				BufferedImage bim = record.getImage(x, y, MAX_TILE_SIZE, MAX_TILE_SIZE, channelNos);
				for(int i = 0; i < factor; i++){
					for(int j = 0; j < factor; j++){
						BufferedImage tile = bim.getSubimage(i*DZI_TILE_SIZE, j*DZI_TILE_SIZE, DZI_TILE_SIZE, DZI_TILE_SIZE);
						int tile_i = start_X/DZI_TILE_SIZE + i;
						int tile_j = start_Y/DZI_TILE_SIZE + j;
						System.out.println(tile_i + "_"+ tile_j);
						count++;
						ImageIO.write(tile, img_format, new File(level_dir.getAbsolutePath() + File.separator + String.valueOf(tile_i)+ "_" + String.valueOf(tile_j) + "." + img_format));
					}
				}
				bim.flush();
				System.out.println("done");
			}
			
		} 
		if(width_extra > 0){
			
			int x_new = record.getWidth() - 1024;
			int y_new = record.getHeight() - 1024;
			for(int y=start_y; y< end_y - height_extra; y+=MAX_TILE_SIZE)
			{
				int start_Y = y;
				BufferedImage bim = record.getImage(x_new, y, MAX_TILE_SIZE, MAX_TILE_SIZE, channelNos);
				BufferedImage c_bim = bim.getSubimage(width-(chunk_width - 1024), 0 , width_extra, MAX_TILE_SIZE);
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
			int y_new  = record.getHeight() - 1024;
			for(int x=start_x; x<end_x - width_extra; x+=MAX_TILE_SIZE)
			{
				int start_X = x;
				BufferedImage bim = record.getImage(x, y_new, MAX_TILE_SIZE, MAX_TILE_SIZE, channelNos);
				BufferedImage c_bim = bim.getSubimage(0, height - (chunk_height - 1024) , MAX_TILE_SIZE, height_extra);
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
			int x_new = record.getWidth() - 1024;
			int y_new = record.getHeight() - 1024;
			BufferedImage bim = record.getImage(x_new, y_new, MAX_TILE_SIZE, MAX_TILE_SIZE, channelNos);
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
	}
	

}
