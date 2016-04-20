package com.graphoscope.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


import com.strandgenomics.imaging.icore.IRecord;
import com.strandgenomics.imaging.icore.util.Util;

import loci.formats.FormatException;


public class TilesCreator {
	
	private final static int MAX_TILE_WIDTH = 4096;
	private static int MAX_TILE_HEIGHT = 4096;
	private static int MAX_ZOOM_LEVEL_FOR_PREFETCHING;
	private static int MIN_ZOOM_LEVEL_FOR_PREFETCHING;
	private static int DZI_TILE_SIZE = 256;
	private static File root;
	private static RecordHolder v;
	public static void main(String[] args) throws FormatException, IOException {
		
		//v = new CreateRecord("/home/ravikiran/FMG_344.tiff");
		v = new RecordHolder("/home/ravikiran/curie_Data/tumour/Normal_005.tif");
		IRecord record = v.getRecord();
		int[] channelNos = {0,1,2};
		String dir = "/home/ravikiran/TestTile/2/";
		int levels = (int) (Math.log(Math.max(v.getHeight(), v.getWidth()))/Math.log(2)) + 1;
		System.out.println(""+ levels);
		
		int width = v.getWidth();
		int height = v.getHeight();
		MAX_ZOOM_LEVEL_FOR_PREFETCHING = (int) Math.floor(Math.log(Math.max(width, height))/Math.log(2) - 8);
		MIN_ZOOM_LEVEL_FOR_PREFETCHING = (int) Math.ceil(Math.log(Math.max(MAX_TILE_WIDTH, MAX_TILE_HEIGHT))/Math.log(2) - 8);
		System.out.println(" max "+ MAX_ZOOM_LEVEL_FOR_PREFETCHING + "min " + MIN_ZOOM_LEVEL_FOR_PREFETCHING );
		
		root = new File("/home/ravikiran/TC");
		for(int i = 0; i <= levels; i++){
			File subLevel = new File(root,String.valueOf(i));
			if(!subLevel.exists()){
				subLevel.mkdir();
			}
		}
		long start = System.currentTimeMillis();
		long[] times = new long[4];
		for(int i=0; i < MIN_ZOOM_LEVEL_FOR_PREFETCHING; i++){
			int level = i;
			int factor = (int) Math.pow(2,level);
			int readSize = DZI_TILE_SIZE*(factor);
			String dirPath = root.getAbsolutePath() + File.separator + (levels - level) + File.separator; 
			System.out.println(dirPath);
			long start0 = System.currentTimeMillis();
			generateTiles(level, readSize, channelNos, dirPath);
			times[i] = System.currentTimeMillis() - start0;
		}  
		doTiling(channelNos);
		System.out.println("time taken total = " + (System.currentTimeMillis() - start));
		
		/*for(int i = 0; i < 4; i++){
			System.out.println("i " + i + "time "+ times[i]);
		}*/

		
	}
	
	public static void doTiling(int[] channeles) throws IOException, FormatException
	{	
		int width = v.getWidth();
		int height = v.getHeight();
		prepareImages(width,height);
		
		long allImagesSize = 0;
		long nTiles = 0; 
		int count = 0;
		long totalTime = 0;
		double estimatedTime = 0, elapsedTime = 0;
		for(int x=0; x < width ;x+=MAX_TILE_WIDTH)
		{
			for(int y=0;y<height;y+=MAX_TILE_HEIGHT)
			{
				nTiles++;
			}
		}
		
		for(int x=0; x < width ;x+=MAX_TILE_WIDTH)
		{
			for(int y=0;y<height;y+=MAX_TILE_HEIGHT)
			{
				System.out.println("Doing for startx="+x+" starty"+y);
				// find out the bounds
				int tileWidth = (x + MAX_TILE_WIDTH) < width ? MAX_TILE_WIDTH : (width - x);
				int tileHeight = (y + MAX_TILE_HEIGHT) < height ? MAX_TILE_HEIGHT : (height - y);
				System.out.println("tile dimension "+tileWidth+" "+tileHeight);
				
				// get the image in original scale
				BufferedImage image = null;
				double curr = System.currentTimeMillis();
				/*int channels = br.getSizeC();
				PixelArray[] pixelArrays = new PixelArray[channels];
				for( int i=0; i<channels; i++){
					BufferedImage img = br.openImage(i,x, y, tileWidth, tileHeight);
					pixelArrays[i] = PixelArray.toPixelArray(img);
				}
				image = PixelArray.getOverlayImage(pixelArrays);*/
				
				image = v.getImage(x, y, tileWidth, tileHeight,channeles);
				double after_read = (System.currentTimeMillis() - curr);
				System.out.println("open image "+ after_read);
				System.out.println("fetched tile for x="+x+" y="+y);
				
				count++;
				allImagesSize = 0;
				int zoomCount = 0;
				totalTime = 0;
				for (int zoom = MAX_ZOOM_LEVEL_FOR_PREFETCHING; zoom >= MIN_ZOOM_LEVEL_FOR_PREFETCHING; zoom--)
				{
					zoomCount++;
					long startTime = System.currentTimeMillis();
					System.out.println("for zoom="+zoom);
					int scalingFactor = (int)Math.pow(2, zoom);
					
					int scaledTileWidth = (int)Math.ceil((double)tileWidth / scalingFactor);
					int scaledTileHeight = (int) Math.ceil((double)tileHeight / scalingFactor);
					
					int scaledTopX = x / scalingFactor;
					int scaledTopY = y / scalingFactor;
					
					//BufferedImage scaledImage = Util.resizeImage(image, scaledTileWidth, scaledTileHeight);
					//writeImage(scaledImage, scaledTopX, scaledTopY, zoom);
					long strttime = System.currentTimeMillis();
					long tmptime = 0;
					BufferedImage imageForZoom = getScaledImageForZoom(zoom);
					tmptime = System.currentTimeMillis() - strttime;
					System.out.println("i "+ tmptime);
					
					
					strttime = System.currentTimeMillis();
					Graphics gfx = imageForZoom.getGraphics();
					tmptime = System.currentTimeMillis() - strttime;
					System.out.println("ii "+ tmptime);
					
					
					strttime = System.currentTimeMillis();
					gfx.drawImage(image, scaledTopX, scaledTopY, scaledTopX+scaledTileWidth, scaledTopY+scaledTileHeight, 0, 0, tileWidth, tileHeight, null);
					tmptime = System.currentTimeMillis() - strttime;
					System.out.println("iii "+ tmptime);
					
					strttime = System.currentTimeMillis();
					gfx.dispose();
					tmptime = System.currentTimeMillis() - strttime;
					System.out.println("iv "+ tmptime);
					
					strttime = System.currentTimeMillis();
					writeImage(imageForZoom, zoom);
					tmptime = System.currentTimeMillis() - strttime;
					System.out.println("v "+ tmptime);
					
					elapsedTime = (System.currentTimeMillis() - startTime);
					System.out.println("total elapsed time " + elapsedTime);
				}
			}
		}
		int levels = (int) (Math.log(Math.max(v.getHeight(), v.getWidth()))/Math.log(2)) + 1;
		for (int zoom = MAX_ZOOM_LEVEL_FOR_PREFETCHING; zoom >= MIN_ZOOM_LEVEL_FOR_PREFETCHING; zoom--){
			BufferedImage image = getScaledImageForZoom(zoom);
			generateTilesFromImage(image, DZI_TILE_SIZE, root.getAbsolutePath() + File.separator + String.valueOf(levels - zoom));
		}
		BufferedImage maxZoomImage = getScaledImageForZoom(MAX_ZOOM_LEVEL_FOR_PREFETCHING);
		int zoom =  MAX_ZOOM_LEVEL_FOR_PREFETCHING + 1;
		BufferedImage img = maxZoomImage;
		while(true){
			
			int scaled_height = (int) Math.ceil((double)img.getHeight()/2);
			int scaled_width = (int) Math.ceil((double)img.getWidth()/2);
			BufferedImage scaled_img = getScaledInstance(img, scaled_width, scaled_height, true);
			
			ImageIO.write(scaled_img, "jpg", new File(root.getAbsolutePath() + File.separator + String.valueOf(levels-zoom) + File.separator + "0_0.jpg" ));
			//writeImage(scaled_img, zoom);
			img = scaled_img;
			if(img.getHeight() == 1 && img.getWidth() == 1){
				break;
			}
			zoom++;
		}
		
		
	}
	private static BufferedImage getScaledImageForZoom(int zoom) throws IOException
	{
		File outputFile = new File(new File("/home/ravikiran/TestTile"), String.valueOf(zoom)+".png");
		return ImageIO.read(outputFile);
	}
	
	private static void writeImage(BufferedImage image, int zoom) throws IOException
	{
		File outputFile = new File(new File("/home/ravikiran/TestTile"), String.valueOf(zoom)+".png");
		ImageIO.write(image, "png", outputFile);
	}
	
	private static void prepareImages(int width,int height) throws IOException
	{
		for (int zoom = MAX_ZOOM_LEVEL_FOR_PREFETCHING; zoom >= MIN_ZOOM_LEVEL_FOR_PREFETCHING; zoom--)
		{
			int scalingFactor = (int)Math.pow(2, zoom);
			
			int scaledRecordWidth = width / scalingFactor;
			int scaledRecordHeight = height / scalingFactor;
			
			BufferedImage scaledImage = new BufferedImage(scaledRecordWidth, scaledRecordHeight, BufferedImage.TYPE_INT_RGB);
			File outputFile = new File(new File("/home/ravikiran/TestTile"), String.valueOf(zoom)+".png");
			ImageIO.write(scaledImage, "png", outputFile);
		}
	}
	private static BufferedImage getScaledInstance(
	        BufferedImage img, int targetWidth,
	        int targetHeight, 
	        boolean higherQuality)
	    {
	        int type =
	            (img.getTransparency() == Transparency.OPAQUE)
	            ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
	        BufferedImage ret = (BufferedImage) img;
	        int w, h;
	        if (higherQuality)
	        {
	            // Use multi-step technique: start with original size, then
	            // scale down in multiple passes with drawImage()
	            // until the target size is reached
	            w = img.getWidth();
	            h = img.getHeight();
	        }
	        else
	        {
	            // Use one-step technique: scale directly from original
	            // size to target size with a single drawImage() call
	            w = targetWidth;
	            h = targetHeight;
	        }

	        do
	        {
	            if (higherQuality && w > targetWidth)
	            {
	                w /= 2;
	                if (w < targetWidth)
	                {
	                    w = targetWidth;
	                }
	            }

	            if (higherQuality && h > targetHeight)
	            {
	                h /= 2;
	                if (h < targetHeight)
	                {
	                    h = targetHeight;
	                }
	            }

	            BufferedImage tmp = new BufferedImage(w, h, type);
	            Graphics2D g2 = tmp.createGraphics();
	            g2.drawImage(ret, 0, 0, w, h, null);
	            g2.dispose();

	            ret = tmp;
	        } while (w != targetWidth || h != targetHeight);

	        return ret;
	    }
	private static void generateTiles(int level,int tileSize,int[] channels,String dir) {

		BufferedImage chunk = null;
		int height = (int) v.getHeight();
		int width = (int) v.getWidth();
		int rows = height/tileSize;
		int columns = width/tileSize;
		int x_end = width%tileSize;
		int y_end = height%tileSize;
		int scale = (int) Math.pow(2, level);
		System.out.println(""+rows + " " + columns);
		int count = 0;
		for(int j = 0; j < columns; j++){
			for(int i =0; i < rows; i++){
				chunk = v.getImage(tileSize*j, tileSize*i, tileSize, tileSize,channels);
				chunk = Util.resizeImage(chunk, tileSize/scale, tileSize/scale);
				try {
					ImageIO.write(chunk, "jpg", new File(dir  + j + "_" + i + ".jpeg"));
					count++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(y_end > 0){
				chunk = v.getImage(tileSize*j, height - y_end, tileSize, y_end, channels);
				chunk = Util.resizeImage(chunk, tileSize/scale, y_end/scale);
				//chunk = createResizedCopy(chunk, requiredWidth/columns, requiredHeight/rows, true);
				try {
					ImageIO.write(chunk, "jpg", new File(dir + j + "_" + rows + ".jpeg"));
					count++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		if(x_end > 0){
			for(int i =0; i < rows; i++){
				chunk = v.getImage(tileSize*columns, tileSize*i, x_end, tileSize, channels);
				chunk = Util.resizeImage(chunk, x_end/scale, tileSize/scale);
				//chunk = createResizedCopy(chunk, requiredWidth/columns, requiredHeight/rows, true);
				try {
					ImageIO.write(chunk, "jpg", new File(dir + columns + "_" + i + ".jpeg"));
					count++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(y_end > 0){
				chunk =  v.getImage(tileSize*columns, height-y_end,x_end, y_end,channels);
				chunk = Util.resizeImage(chunk, x_end/scale, y_end/scale);
				//chunk = createResizedCopy(chunk, requiredWidth/columns, requiredHeight/rows, true);
				try {
					ImageIO.write(chunk, "jpg", new File(dir + columns + "_" + rows + ".jpeg"));
					count++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		


	}
	
	private static void generateTilesFromImage(BufferedImage img,int tileSize,String dir) {

		BufferedImage chunk = null;
		int height = img.getHeight();
		int width = img.getWidth();
		int rows = height/tileSize;
		int columns = width/tileSize;
		int x_end = width%tileSize;
		int y_end = height%tileSize;
		System.out.println(""+rows + " " + columns);
		int count = 0;
		for(int j = 0; j < columns; j++){
			for(int i =0; i < rows; i++){
				chunk = img.getSubimage(tileSize*j, tileSize*i, tileSize, tileSize);
				try {
					ImageIO.write(chunk, "jpg", new File(dir  + File.separator + j + "_" + i + ".jpeg"));
					count++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(y_end > 0){
				chunk = img.getSubimage(tileSize*j, height - y_end, tileSize, y_end);
				//chunk = createResizedCopy(chunk, requiredWidth/columns, requiredHeight/rows, true);
				try {
					ImageIO.write(chunk, "jpg", new File(dir + File.separator + j + "_" + rows + ".jpeg"));
					count++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		for(int i =0; i < rows; i++){
			chunk = img.getSubimage(tileSize*columns, tileSize*i, x_end, tileSize);
			//chunk = createResizedCopy(chunk, requiredWidth/columns, requiredHeight/rows, true);
			try {
				ImageIO.write(chunk, "jpg", new File(dir + File.separator + columns + "_" + i + ".jpeg"));
				count++;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(y_end > 0){
			chunk = img.getSubimage(tileSize*columns, height-y_end,x_end, y_end);
			//chunk = createResizedCopy(chunk, requiredWidth/columns, requiredHeight/rows, true);
			try {
				ImageIO.write(chunk, "jpg", new File(dir + File.separator + columns + "_" + rows + ".jpeg"));
				count++;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}
	
}
