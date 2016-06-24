package com.strandgenomics.imaging.graphoscope.tiling;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.strandgenomics.imaging.icore.util.Util;

public class StitchingThread extends Thread {
	private static int DZI_TILE_SIZE = 256;
	private int start_col;
	private int start_row;
	private int end_col;
	private int end_row;
	private String inp_dir;
	private String out_dir;
	private String img_format;
	public StitchingThread(String label,String inp_dir, String out_dir,int start_col, int start_row, int end_col, int end_row, String format){
		super("thread '" + label + "'");
		this.start_col = start_col;
		this.start_row = start_row;
		this.end_col = end_col;
		this.end_row = end_row;
		this.img_format = format;
		this.inp_dir = inp_dir;
		this.out_dir = out_dir;
	}
	public void run () {
		System.out.println(start_col + "");
		try {
			//inp_dir = "/home/ravikiran/SC/12";
			//out_dir = "/home/ravikiran/SC/11";
			doStitching(inp_dir, start_row, end_row, start_col,end_col, out_dir);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void doStitching(String inp_dir, int start_row,int end_row,int start_col,int end_col,String out_dir) throws IOException{
		
		File inp_folder = new File(inp_dir);
		File output_folder = new File(out_dir);
		if(!output_folder.exists())
			output_folder.mkdir();
		int count = 0;
		int rows = end_row - start_row;
		int columns = end_col - start_col;
		if(rows == 0 || columns == 0)
			return;
		int t_rows = rows;
		int t_cols = columns;
		if(rows%2 == 1){
			t_rows = rows - 1;
		}
		if(columns%2 == 1){
			t_cols  = columns -1;
		}
		for(int i = start_col; i < start_col + t_cols; i= i + 2){
			for(int j = start_row; j < start_row + t_rows; j = j + 2){

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
		if(rows%2 != 0){
			for(int i = start_col; i < start_col + t_cols; i = i + 2 ){
				BufferedImage img1 = ImageIO.read(new File(inp_folder, ""+ i + "_" + (start_row + t_rows) +  "." + img_format));
				BufferedImage img2 = ImageIO.read(new File(inp_folder, ""+ (i + 1) + "_" + (start_row + t_rows) +  "." + img_format));
				int img1_scaled_width = (int) Math.ceil((float)img1.getWidth()/2);
				int img1_scaled_height = (int) Math.ceil((float)img1.getHeight()/2);
				int img2_scaled_width = (int) Math.ceil((float)img2.getWidth()/2);
				int img2_scaled_height = (int) Math.ceil((float)img2.getHeight()/2);
				BufferedImage finalimg = new BufferedImage(img1_scaled_width + img2_scaled_width,img1_scaled_height,BufferedImage.TYPE_INT_RGB);
				Graphics g = finalimg.getGraphics();
				g.drawImage(img1, 0, 0, img1_scaled_width, img1_scaled_height, 0, 0, img1.getWidth(), img1.getHeight(), null);
				g.drawImage(img2,img1_scaled_width, 0, img1_scaled_width + img2_scaled_width, img2_scaled_height, 0, 0, img2.getWidth(), img2.getHeight(), null);
				g.dispose();
				ImageIO.write(finalimg, img_format, new File(output_folder.getAbsolutePath()  + File.separator + i/2 + "_"+ (start_row + t_rows)/2 +  "." + img_format));
			}
		}
		if(columns%2 != 0){
			for(int i = start_row; i < start_row + t_rows; i = i + 2 ){
				BufferedImage img1 = ImageIO.read(new File(inp_folder, ""+  (start_col + t_cols) + "_" + i +  "." + img_format));
				BufferedImage img2 = ImageIO.read(new File(inp_folder, ""+  (start_col + t_cols) + "_" + (i + 1) +  "." + img_format));
				int img1_scaled_width = (int) Math.ceil((float)img1.getWidth()/2);
				int img1_scaled_height = (int) Math.ceil((float)img1.getHeight()/2);
				int img2_scaled_width = (int) Math.ceil((float)img2.getWidth()/2);
				int img2_scaled_height = (int) Math.ceil((float)img2.getHeight()/2);
				BufferedImage finalimg = new BufferedImage(img1_scaled_width,img2_scaled_height +img1_scaled_height,BufferedImage.TYPE_INT_RGB);
				Graphics g = finalimg.getGraphics();
				g.drawImage(img1, 0, 0, img1_scaled_width, img1_scaled_height, 0, 0, img1.getWidth(), img1.getHeight(), null);
				g.drawImage(img2, 0,img1_scaled_height, img1_scaled_width, img1_scaled_height  + img2_scaled_height, 0, 0, img2.getWidth(), img2.getHeight(), null);
				g.dispose();
				ImageIO.write(finalimg, img_format, new File(output_folder.getAbsolutePath()  + File.separator + (start_col + t_cols)/2 + "_"+ i/2 + "." + img_format));
			}
		}
		if(rows%2!=0 && columns%2 != 0){
			BufferedImage img = ImageIO.read(new File(inp_folder, ""+ (start_col + t_cols) + "_" + (start_row + t_rows) +  "." + img_format));
			int img1_scaled_width = (int) Math.ceil((float)img.getWidth()/2);
			int img1_scaled_height = (int) Math.ceil((float)img.getHeight()/2);
			img = Util.resizeImage(img,img1_scaled_width,  img1_scaled_height);
			ImageIO.write(img, img_format, new File(output_folder.getAbsolutePath()  + File.separator + (start_col +t_cols)/2 + "_"+ (start_row +t_rows)/2 + "." + img_format));
		}
	}

}
