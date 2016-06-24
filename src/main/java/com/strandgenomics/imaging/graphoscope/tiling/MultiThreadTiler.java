package com.strandgenomics.imaging.graphoscope.tiling;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.strandgenomics.imaging.iclient.ImageSpaceSystem;
import com.strandgenomics.imaging.iclient.Record;
import com.strandgenomics.imaging.icore.Constants;
import com.strandgenomics.imaging.graphoscope.tiling.Stitcher;

public class MultiThreadTiler {
	int readers;
	int cores;
	int threads;
	private long recordId;
	private ImageSpaceSystem ispace;
	private File storageroot;
	private RecordParameters recordParams;
	public MultiThreadTiler(long recordId,ImageSpaceSystem iSpace,File storageroot,RecordParameters recordParams){
		cores = Runtime.getRuntime().availableProcessors();
		readers = Constants.getBufferedReaderStackSize();
		//readers = 4;
		threads = 4*cores;
		this.recordId = recordId;
		this.ispace = iSpace;
		this.storageroot = storageroot;
		this.recordParams = recordParams;
	}
	public void executeTiling() throws IOException{
		Record record = ispace.findRecordForGUID(recordId);
		int height = record.getImageHeight();
		int width = record.getImageWidth();
		String img_format = "png";
		
		int levels = (int) (Math.log(Math.max(height, width))/Math.log(2)) + 1;
		String root = createDirectory(levels,  width, height);
		
		Tiling t = new Tiling(root,recordId, levels, img_format,ispace, recordParams);
		TilerThread[] t_s = new TilerThread[threads];
		int[] channelNos = {0,1,2};
		int size = Math.max(width,height);
		
		if(width > height){
			int div = size/threads;
			div = div - div%1024;
			System.out.println("div " + div );
			for(int i = 0; i < threads; i++){
				if(i != threads - 1)
					t_s[i] = new TilerThread("Thread " + i,0,t,i*div, 0, div*(i+1), height,channelNos); 
				else
					t_s[i] = new TilerThread("Thread " + i,0,t,i*div, 0, width, height,channelNos);
			}
		}
		else{
			int div = size/threads;
			div = div - div%1024;
			System.out.println("div " + div );
			for(int i = 0; i < threads; i++){
				if(i != threads - 1)
					t_s[i] = new TilerThread("Thread " + i,0,t,0, i*div, width, (i+1)*div,channelNos); 
				else
					t_s[i] = new TilerThread("Thread " + i,0,t,0, i*div, width, height,channelNos);
			}
		}
		for(int i = 0; i < threads; i++){
			t_s[i].start();
		}
		for(int i = 0; i < threads; i++){
			try {
				t_s[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
		}
		//create all the lower levels from highest level
		Stitcher s = new Stitcher(root,record.getImageWidth(), record.getImageHeight(), levels,img_format);
		s.createAllLevels();
	}
	private String createDirectory(int levels, int width,int height){
		File record_dir = new File(storageroot, "" + recordId);
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
	private File createXML(String dir,int width,int height){
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
		return storageroot;
		
	}
}
