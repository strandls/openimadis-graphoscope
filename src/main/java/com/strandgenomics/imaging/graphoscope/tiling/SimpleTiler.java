package com.strandgenomics.imaging.graphoscope.tiling;


import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.strandgenomics.imaging.iclient.ImageSpaceSystem;
import com.strandgenomics.imaging.iclient.Record;

public class SimpleTiler {
	
	private final int DZI_TILE_SIZE = 256;
	private  String img_format = "png";
	private  File storageRoot;
	
	private long recordId;
	private ImageSpaceSystem ispace;
	private RecordParameters recordParams;
	public SimpleTiler(File storage,long recordId, ImageSpaceSystem iSpace, RecordParameters params){
		this.storageRoot = storage;
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
		Record record = ispace.findRecordForGUID(recordId);
		
		
		int height = (int) record.getImageHeight();
		int width = (int) record.getImageWidth();
		int levels = (int) (Math.log(Math.max(height, width))/Math.log(2)) + 1;
		File recordFilesDir = new File(createDirectory(levels,width,height));
		File level_dir = new File(recordFilesDir.getAbsolutePath() + File.separator + String.valueOf(levels));
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
		Stitcher s = new Stitcher(recordFilesDir.getAbsolutePath(),record.getImageWidth(), record.getImageHeight(), levels,img_format);
		try {
			s.createAllLevels();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("count" + count);
		System.out.println("leaving/////////////////////////////////");
	}
	private String createDirectory(int levels, int width,int height){
		File record_dir = new File(storageRoot, "" + recordId);
		if(!record_dir.exists()){
			record_dir.mkdir();
		}
		File recordFiles_dir = new File(record_dir, recordId + "_files");
		if(!recordFiles_dir.exists()){
			recordFiles_dir.mkdir();
		}
		createXML(record_dir.getAbsolutePath(),width,height);
		String root = recordFiles_dir.getAbsolutePath();
		
		for(int i = 0; i <= levels; i++){
			File subLevel = new File(root,String.valueOf(i));
			if(!subLevel.exists()){
				subLevel.mkdir();
			}
		}
		return root;
	}
	private void createXML(String dir,int width,int height){
		InputStream is = this.getClass().getResourceAsStream("/dziformat.xml");
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	      try {
	         // use the factory to create a documentbuilder
	         DocumentBuilder builder = factory.newDocumentBuilder();

	         // create a new document from input stream
	         Document doc = builder.parse(is);
	         NodeList nodeList = doc.getElementsByTagName("Image");
	         //System.out.println(nodeList.item(0).getAttributes().getNamedItem("Format").getNodeValue());
	         nodeList.item(0).getAttributes().getNamedItem("Format").setNodeValue("png");
	         nodeList.item(0).getAttributes().getNamedItem("TileSize").setNodeValue("256");
	         nodeList = doc.getElementsByTagName("Size");
	         nodeList.item(0).getAttributes().getNamedItem("Height").setNodeValue(String.valueOf(height));
	         nodeList.item(0).getAttributes().getNamedItem("Width").setNodeValue(String.valueOf(width));
	         
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	 		Transformer transformer = transformerFactory.newTransformer();
	 		DOMSource source = new DOMSource(doc);
	 		File dziDir = new File(dir);
	 		File dziFile = new File(dziDir,recordId + ".dzi");
	 		StreamResult result = new StreamResult(dziFile);
	 		transformer.transform(source, result);
	 		 
	      } catch (Exception ex) {
	         ex.printStackTrace();
	      }
	}

}

