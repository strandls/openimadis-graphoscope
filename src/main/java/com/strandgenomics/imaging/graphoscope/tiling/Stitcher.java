package com.strandgenomics.imaging.graphoscope.tiling;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.strandgenomics.imaging.icore.util.Util;


public class Stitcher {
		private static int DZI_TILE_SIZE = 256;
		private File root = null;
		private int record_width;
		private int record_height;
		private int max_level;
		private static String img_format;
		
		public Stitcher(String root_dir, int width, int height, int level, String format){
			root = new File(root_dir);
			System.out.println(root.getAbsolutePath());
			record_width = width;
			record_height = height;
			max_level = level;
			img_format = format;
		}
		
		public void threading( String inp_dir,int width,int height, String out_dir){
			
			int rows = (int) Math.ceil((float)height/DZI_TILE_SIZE);
			int columns = (int) Math.ceil((float)width/DZI_TILE_SIZE);
			int mid_y=0, mid_x=0;
			
			if(rows % 4 == 0){
				mid_y = rows/2;
			}
			else if(rows % 4 == 1){
				mid_y = rows/2;
			}
			else if(rows % 4 == 2){
				mid_y = rows/2 - 1;
			}
			else if(rows % 4 == 3){
				mid_y = rows/2 -1;
			}
			
			if(columns % 4 == 0){
				mid_x = columns/2;
			}
			else if(columns % 4 == 1){
				mid_x = columns/2;
			}
			else if(columns % 4 == 2){
				mid_x = columns/2 - 1;
			}
			else if(columns % 4 == 3){
				mid_x = columns/2 -1;
			}
			// 4 threads
			StitchingThread s1 = new StitchingThread("A",inp_dir, out_dir, 0, 0, mid_x, mid_y,img_format );
			StitchingThread s2 = new StitchingThread("B",inp_dir, out_dir, mid_x, 0, columns, mid_y,img_format );	
			StitchingThread s3 = new StitchingThread("C",inp_dir, out_dir, 0, mid_y, mid_x, rows,img_format );	
			StitchingThread s4 = new StitchingThread("D",inp_dir, out_dir, mid_x, mid_y, columns,rows ,img_format );
			s1.start();
			s2.start();
			s3.start();
			s4.start();
			try {
				s1.join();
				s2.join();
				s3.join();
				s4.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void createAllLevels() throws IOException{
			String inp_dir = null;
			String out_dir = null;
			int width = record_width;
			int height = record_height;
			int thumb_level = 0;
			for(int i = max_level -1; i > 0; i--){
				thumb_level = i;
				
				inp_dir = root.getAbsolutePath() + File.separator + String.valueOf(i+1);
				out_dir = root.getAbsolutePath() + File.separator + String.valueOf(i);
				//stitcher(inp_dir,width,height,out_dir);
				threading(inp_dir, width, height, out_dir);
				
				width = (int) Math.ceil((float)width/2);
				height = (int) Math.ceil((float)height/2);
				if(width < 256 && height < 256 )
					break;
				
			}
			generateThumbnails(thumb_level);
		}
		private void generateThumbnails(int thumbnailLevel) throws IOException{
			File thumb_dir = new File(root.getAbsolutePath() + File.separator + String.valueOf(thumbnailLevel));
			thumb_dir = new File(thumb_dir,"0_0" + "." + img_format);
			BufferedImage thumb = ImageIO.read(thumb_dir);
			int zoom =  thumbnailLevel - 1;
			BufferedImage img = thumb;
			while(true){
				
				int scaled_height = (int) Math.ceil((double)img.getHeight()/2);
				int scaled_width = (int) Math.ceil((double)img.getWidth()/2);
				BufferedImage scaled_img = Util.resizeImage(img, scaled_width, scaled_height);
				
				ImageIO.write(scaled_img, img_format, new File(root.getAbsolutePath() + File.separator + String.valueOf(zoom) + File.separator + "0_0." + img_format ));
				//writeImage(scaled_img, zoom);
				img = scaled_img;
				if(zoom == 0){
					break;
				}
				zoom--;
			}
		}
}
