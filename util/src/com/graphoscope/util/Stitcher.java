package com.graphoscope.util;


import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import com.strandgenomics.imaging.icore.util.Util;


public class Stitcher {
public static void  main(String[] args) throws IOException{
		
		//int height = (int) v.getHeight();
		//int width = (int) v.getWidth();
		int rows = 3;
		int columns = 6;
		int t_rows = rows;
		int t_cols = columns;
		if(rows%2 == 1){
			t_rows = rows - 1;
		}
		if(columns%2 == 1){
			t_cols  = columns -1;
		}
		int tileSize = 256;
		BufferedImage chunk = null;
		File folder = new File("/home/ravikiran/bigBIF/11");
		int files_count = folder.list().length;
		String[] dirListing = null;
		dirListing = folder.list();
		/*for(int i = 0; i < columns; i= i + 2){
			for(int j = 0; j < rows; j = j + 2){
				
				File tmp = new File(folder, ""+ i + "_" + j);
				
				BufferedImage img1 = ImageIO.read(new File(folder, ""+ i + "_" + j + ".jpeg"));
				BufferedImage img2 = ImageIO.read(new File(folder, ""+ (i + 1) + "_" + j + ".jpeg"));
				BufferedImage img3 = ImageIO.read(new File(folder,  ""+i + "_" + (j + 1) + ".jpeg"));
				BufferedImage img4 = ImageIO.read(new File(folder, ""+ (i + 1) + "_" + (j + 1) + ".jpeg"));
				
				BufferedImage finalimg = new BufferedImage(img1.getWidth()/2 + img4.getWidth()/2,img1.getHeight()/2 + img4.getHeight()/2,BufferedImage.TYPE_INT_RGB);
				Graphics g = finalimg.getGraphics();
				g.drawImage(img1, 0, 0, img1.getWidth()/2, img1.getHeight()/2, 0, 0, img1.getWidth(), img1.getHeight(), null);
				g.drawImage(img3, 0, img1.getHeight()/2, img3.getWidth()/2, img3.getHeight()/2+img1.getHeight()/2, 0, 0, img3.getWidth(), img3.getHeight(), null);
				g.drawImage(img2, img1.getWidth()/2, 0, img1.getWidth()/2 + img2.getWidth()/2, img2.getHeight()/2, 0, 0, img2.getWidth(), img2.getHeight(), null);
				g.drawImage(img4, img1.getWidth()/2, img1.getHeight()/2, img1.getWidth()/2 + img4.getWidth()/2, img1.getHeight()/2 + img4.getHeight()/2, 0, 0, img4.getWidth(), img4.getHeight(), null);
				g.dispose();
				ImageIO.write(finalimg, "jpeg", new File("/home/ravikiran/bigBIF/10_" + File.separator + i + "_"+ j + ".jpeg"));
				System.out.println(tmp.getAbsolutePath());
			}
		} */
		if(rows%2 != 0){
			for(int i = 0; i < t_cols; i = i + 2 ){
				BufferedImage img1 = ImageIO.read(new File(folder, ""+ i + "_" + t_rows + ".jpeg"));
				BufferedImage img2 = ImageIO.read(new File(folder, ""+ (i + 1) + "_" + t_rows + ".jpeg"));
				BufferedImage finalimg = new BufferedImage(img1.getWidth()/2 + img2.getWidth()/2,img1.getHeight()/2,BufferedImage.TYPE_INT_RGB);
				Graphics g = finalimg.getGraphics();
				g.drawImage(img1, 0, 0, img1.getWidth()/2, img1.getHeight()/2, 0, 0, img1.getWidth(), img1.getHeight(), null);
				g.drawImage(img2, img1.getWidth()/2, 0, img1.getWidth()/2 + img2.getWidth()/2, img2.getHeight()/2, 0, 0, img2.getWidth(), img2.getHeight(), null);
				g.dispose();
				ImageIO.write(finalimg, "jpeg", new File("/home/ravikiran/bigBIF/10_" + File.separator + i + "_"+ t_rows + ".jpeg"));
			}
		}
		if(columns%2 != 0){
			for(int i = 0; i < t_rows; i = i + 2 ){
				BufferedImage img1 = ImageIO.read(new File(folder, ""+ t_cols + "_" + i + ".jpeg"));
				BufferedImage img2 = ImageIO.read(new File(folder, ""+ t_cols + "_" + (i + 1) + ".jpeg"));
				BufferedImage finalimg = new BufferedImage(img1.getWidth()/2,img2.getHeight()/2 +img1.getHeight()/2,BufferedImage.TYPE_INT_RGB);
				Graphics g = finalimg.getGraphics();
				g.drawImage(img1, 0, 0, img1.getWidth()/2, img1.getHeight()/2, 0, 0, img1.getWidth(), img1.getHeight(), null);
				g.drawImage(img2, 0,img1.getHeight()/2, img1.getWidth()/2, img1.getHeight()/2 + img2.getHeight()/2, 0, 0, img2.getWidth(), img2.getHeight(), null);
				g.dispose();
				ImageIO.write(finalimg, "jpeg", new File("/home/ravikiran/bigBIF/10_" + File.separator + t_cols + "_"+ i + ".jpeg"));
			}
		}
		if(rows%2!=0 && columns%2 != 0){
			BufferedImage img = ImageIO.read(new File(folder, ""+ t_cols + "_" + t_rows + ".jpeg"));
			Util.resizeImage(img, img.getWidth(), img.getWidth());
			ImageIO.write(img, "jpeg", new File("/home/ravikiran/bigBIF/10_" + File.separator + t_cols + "_"+ t_rows + ".jpeg"));
		}
		File e = new File(folder,""+ 0 + "_" + (0 + 1) + ".jpeg" );
		BufferedImage g = ImageIO.read(e);
		System.out.println(""+ g.getHeight());
	}
}
