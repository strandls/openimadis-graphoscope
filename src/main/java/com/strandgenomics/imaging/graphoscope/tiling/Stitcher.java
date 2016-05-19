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
			record_width = width;
			record_height = height;
			max_level = level;
			img_format = format;
		}
		public static void  stitcher(String inp_dir, int width,int height,String out_dir) throws IOException{

		
			int rows = (int) Math.ceil((float)height/DZI_TILE_SIZE);
			int columns = (int) Math.ceil((float)width/DZI_TILE_SIZE);
			int t_rows = rows;
			int t_cols = columns;
			if(rows%2 == 1){
				t_rows = rows - 1;
			}
			if(columns%2 == 1){
				t_cols  = columns -1;
			}	
			File inp_folder = new File(inp_dir);
			File output_folder = new File(out_dir);
			if(!output_folder.exists())
				output_folder.mkdir();
			int count = 0;
			for(int i = 0; i < t_cols; i= i + 2){
				for(int j = 0; j < t_rows; j = j + 2){

					File tmp = new File(inp_folder, ""+ i + "_" + j);

					BufferedImage img1 = ImageIO.read(new File(inp_folder, ""+ i + "_" + j +  "." + img_format));
					BufferedImage img2 = ImageIO.read(new File(inp_folder, ""+ (i + 1) + "_" + j +  "." + img_format));
					BufferedImage img3 = ImageIO.read(new File(inp_folder,  ""+i + "_" + (j + 1) +  "." + img_format));
					BufferedImage img4 = ImageIO.read(new File(inp_folder, ""+ (i + 1) + "_" + (j + 1) + "." +  img_format));
					int img1_scaled_width = (int) Math.ceil((float)img1.getWidth()/2);
					int img1_scaled_height = (int) Math.ceil((float)img1.getHeight()/2);
					int img2_scaled_width = (int) Math.ceil((float)img2.getWidth()/2);
					int img2_scaled_height = (int) Math.ceil((float)img2.getHeight()/2);
					int img3_scaled_width = (int) Math.ceil((float)img3.getWidth()/2);
					int img3_scaled_height = (int) Math.ceil((float)img3.getHeight()/2);
					int img4_scaled_width = (int) Math.ceil((float)img4.getWidth()/2);
					int img4_scaled_height = (int) Math.ceil((float)img4.getHeight()/2);
					BufferedImage finalimg = new BufferedImage(img1_scaled_width + img4_scaled_width, img1_scaled_height +  img4_scaled_height,BufferedImage.TYPE_INT_RGB);
					Graphics g = finalimg.getGraphics();
					g.drawImage(img1, 0, 0, img1_scaled_width, img1_scaled_height, 0, 0, img1.getWidth(), img1.getHeight(), null);
					g.drawImage(img3, 0, img1_scaled_height, img3_scaled_width, img3.getHeight()/2+img1.getHeight()/2, 0, 0, img3.getWidth(), img3.getHeight(), null);
					g.drawImage(img2, img1_scaled_width, 0, img1_scaled_width + img2_scaled_width, img2_scaled_height, 0, 0, img2.getWidth(), img2.getHeight(), null);
					g.drawImage(img4, img1_scaled_width, img1_scaled_width, img1_scaled_width + img4_scaled_width, img1_scaled_height + img4_scaled_height, 0, 0, img4.getWidth(), img4.getHeight(), null);
					g.dispose();
					ImageIO.write(finalimg, img_format, new File(output_folder.getAbsolutePath() + File.separator + i/2 + "_"+ j/2 +  "." + img_format));
					System.out.println(tmp.getAbsolutePath());
					count++;
				}
			} 
			System.out.println("count "+ count);
			if(rows%2 != 0){
				for(int i = 0; i < t_cols; i = i + 2 ){
					BufferedImage img1 = ImageIO.read(new File(inp_folder, ""+ i + "_" + t_rows +  "." + img_format));
					BufferedImage img2 = ImageIO.read(new File(inp_folder, ""+ (i + 1) + "_" + t_rows +  "." + img_format));
					int img1_scaled_width = (int) Math.ceil((float)img1.getWidth()/2);
					int img1_scaled_height = (int) Math.ceil((float)img1.getHeight()/2);
					int img2_scaled_width = (int) Math.ceil((float)img2.getWidth()/2);
					int img2_scaled_height = (int) Math.ceil((float)img2.getHeight()/2);
					BufferedImage finalimg = new BufferedImage(img1_scaled_width + img2_scaled_width,img1_scaled_height,BufferedImage.TYPE_INT_RGB);
					Graphics g = finalimg.getGraphics();
					g.drawImage(img1, 0, 0, img1_scaled_width, img1_scaled_height, 0, 0, img1.getWidth(), img1.getHeight(), null);
					g.drawImage(img2,img1_scaled_width, 0, img1_scaled_width + img2_scaled_width, img2_scaled_height, 0, 0, img2.getWidth(), img2.getHeight(), null);
					g.dispose();
					ImageIO.write(finalimg, img_format, new File(output_folder.getAbsolutePath()  + File.separator + i/2 + "_"+ t_rows/2 +  "." + img_format));
				}
			}
			if(columns%2 != 0){
				for(int i = 0; i < t_rows; i = i + 2 ){
					BufferedImage img1 = ImageIO.read(new File(inp_folder, ""+ t_cols + "_" + i +  "." + img_format));
					BufferedImage img2 = ImageIO.read(new File(inp_folder, ""+ t_cols + "_" + (i + 1) +  "." + img_format));
					int img1_scaled_width = (int) Math.ceil((float)img1.getWidth()/2);
					int img1_scaled_height = (int) Math.ceil((float)img1.getHeight()/2);
					int img2_scaled_width = (int) Math.ceil((float)img2.getWidth()/2);
					int img2_scaled_height = (int) Math.ceil((float)img2.getHeight()/2);
					BufferedImage finalimg = new BufferedImage(img1_scaled_width,img2_scaled_height +img1_scaled_height,BufferedImage.TYPE_INT_RGB);
					Graphics g = finalimg.getGraphics();
					g.drawImage(img1, 0, 0, img1_scaled_width, img1_scaled_height, 0, 0, img1.getWidth(), img1.getHeight(), null);
					g.drawImage(img2, 0,img1_scaled_height, img1_scaled_width, img1_scaled_height  + img2_scaled_height, 0, 0, img2.getWidth(), img2.getHeight(), null);
					g.dispose();
					ImageIO.write(finalimg, img_format, new File(output_folder.getAbsolutePath()  + File.separator + t_cols/2 + "_"+ i/2 + "." + img_format));
				}
			}
			if(rows%2!=0 && columns%2 != 0){
				BufferedImage img = ImageIO.read(new File(inp_folder, ""+ t_cols + "_" + t_rows +  "." + img_format));
				int img1_scaled_width = (int) Math.ceil((float)img.getWidth()/2);
				int img1_scaled_height = (int) Math.ceil((float)img.getHeight()/2);
				img = Util.resizeImage(img,img1_scaled_width,  img1_scaled_height);
				ImageIO.write(img, img_format, new File(output_folder.getAbsolutePath()  + File.separator + t_cols/2 + "_"+ t_rows/2 + "." + img_format));
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
				stitcher(inp_dir,width,height,out_dir);
				width = (int) Math.ceil((float)width/2);
				height = (int) Math.ceil((float)height/2);
				if(width < 256 && height < 256 )
					break;
				
			}
			File thumb_dir = new File(root.getAbsolutePath() + File.separator + String.valueOf(thumb_level));
			thumb_dir = new File(thumb_dir,"0_0" + "." + img_format);
			BufferedImage thumb = ImageIO.read(thumb_dir);
			int zoom =  thumb_level - 1;
			BufferedImage img = thumb;
			while(true){
				
				int scaled_height = (int) Math.ceil((double)img.getHeight()/2);
				int scaled_width = (int) Math.ceil((double)img.getWidth()/2);
				BufferedImage scaled_img = Util.resizeImage(img, scaled_width, scaled_height);
				
				ImageIO.write(scaled_img, img_format, new File(root.getAbsolutePath() + File.separator + String.valueOf(zoom) + File.separator + "0_0." + img_format ));
				//writeImage(scaled_img, zoom);
				img = scaled_img;
				if(img.getHeight() == 1 && img.getWidth() == 1){
					break;
				}
				zoom--;
			}
		}
}