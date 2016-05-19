package com.strandgenomics.imaging.graphoscope;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.gson.Gson;
import com.strandgenomics.imaging.graphoscope.tiling.MultiThreadTiler;
import com.strandgenomics.imaging.graphoscope.tiling.RecordParameters;
import com.strandgenomics.imaging.iclient.AuthenticationException;
import com.strandgenomics.imaging.iclient.ImageSpaceObject;
import com.strandgenomics.imaging.iclient.ImageSpaceSystem;
import com.strandgenomics.imaging.iclient.Record;
import com.strandgenomics.imaging.iclient.impl.ws.ispace.Area;
import com.strandgenomics.imaging.icore.Channel;
import com.strandgenomics.imaging.icore.Dimension;
import com.strandgenomics.imaging.icore.VisualContrast;
import com.strandgenomics.imaging.icore.image.Histogram;
import com.strandgenomics.imaging.tileviewer.Helper;
import com.strandgenomics.imaging.tileviewer.system.Tiling;


public class Viewer {

	private String appId;
	private File storageRoot;
	private Map<Long, String> contrastMap;

	/**
	 * @param context
	 */
	public Viewer(ServletContext context)
	{
		Properties prop = new Properties();
		try
		{
			System.out.println(context.getRealPath("WEB-INF/client.properties"));
			prop.load(new FileInputStream(context.getRealPath("WEB-INF/client.properties")));
			appId = prop.getProperty("clientId");
			System.out.println("clientid="+prop.getProperty("clientId"));
			String storagePath = prop.getProperty("cacheDir");
			this.storageRoot = new File(storagePath);
			
			System.setProperty("cache_storage_root", this.storageRoot.getAbsolutePath());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		contrastMap = new HashMap<Long, String>();
	}
	
	
	
	




	/**
	 * get a certain set of data for a record
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	

	/**
	 * log in to server using auth-code and client API
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		ImageSpaceSystem iSpace = ImageSpaceObject.getImageSpace();
		String token = null;
		boolean login = false;
		JSONObject json = new JSONObject();

		String authCode = Helper.getRequiredParam(Helper.AUTH_CODE, request);
		String host = Helper.getRequiredParam(Helper.HOST, request);
		int port = Integer.parseInt(Helper.getOptionalParam(Helper.PORT, request, "80"));
		boolean scheme = Helper.getOptionalParam(Helper.SCHEME, request, "http").compareTo("https") == 0;
		
		try
		{
			for (int i = 0; i < 3 && !login; i++)
			{
				System.out.println("Login try: " + (i + 1));
				
				System.out.println("clientId="+appId+" dir="+storageRoot);
				login = iSpace.login(scheme, host, port, appId, authCode);
				token = iSpace.getAccessKey();
			}
		}
		catch (AuthenticationException e)
		{
			e.printStackTrace();
		}

		try
		{
			json.put("login", login);
			if (login)
			{
				json.put("token", token);
			}
		}
		catch (JSONException e)
		{
			System.err.println("JSON (login): " + e.getMessage());
		}

		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.println(json.toString());
		out.close();
	}
	
	public void startTiling(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		long recordid = Long.parseLong(Helper.getRequiredParam(Helper.RECORD_ID, request));
		String token = Helper.getRequiredParam(Helper.TOKEN, request);
		String host = Helper.getRequiredParam(Helper.HOST, request);
		int port = Integer.parseInt(Helper.getOptionalParam(Helper.PORT, request, "80"));
		boolean scheme = Helper.getOptionalParam(Helper.SCHEME, request, "http").compareTo("https") == 0;

		int frameNumber = Integer.parseInt(Helper.getOptionalParam(Helper.FRAME_NUMBER, request, "0"));
		int sliceNumber = Integer.parseInt(Helper.getOptionalParam(Helper.SLICE_NUMBER, request, "0"));
		int channelCount = Integer.parseInt(Helper.getOptionalParam(Helper.CHANNEL_NUMBERS, request, "1"));

		boolean isGrayScale = Integer.parseInt(Helper.getOptionalParam(Helper.GREY_SCALE, request, "0")) == 1;
		boolean isZStacked = Integer.parseInt(Helper.getOptionalParam(Helper.Z_STACKED, request, "0")) == 1;
		
		RecordParameters params = new RecordParameters(frameNumber, sliceNumber, channelCount, isGrayScale, isZStacked, null);
		ImageSpaceSystem iSpace = null;
		iSpace = ImageSpaceObject.getImageSpace();
		iSpace.setAccessKey(scheme, host, port, token);
		doTiling(recordid,iSpace,params);
	}
	public void doTiling(long guid, ImageSpaceSystem ispace, RecordParameters recordParams) throws IOException{
		System.out.println("doTiling................." + guid);
//		BufferedImage img = ispace.getOverlayedImage(guid, 0, 0, 0, channelNos, false, false, true, 0, 0, 512, 512);
//		ImageIO.write(img, "png", new File("/home/ravikiran/viewer.png"));
		/*com.strandgenomics.imaging.graphviewer.tiling.Tiling t = new com.strandgenomics.imaging.graphviewer.tiling.Tiling("/home/ravikiran/imanage/graphviewer_cache/", guid, 0, "png", ispace);
		t.doTiling(0, 0, 1024, 1024, channelNos);*/
		
		MultiThreadTiler m = new MultiThreadTiler(guid, ispace,storageRoot,recordParams);
		m.executeTiling();
		
	}
	public void getRecordData(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String token = Helper.getRequiredParam(Helper.TOKEN, request);
		String host = Helper.getRequiredParam(Helper.HOST, request);
		int port = Integer.parseInt(Helper.getOptionalParam(Helper.PORT, request, "80"));
		boolean scheme = Helper.getOptionalParam(Helper.SCHEME, request, "http").compareTo("https") == 0;

		ImageSpaceSystem iSpace = null;
		iSpace = ImageSpaceObject.getImageSpace();
		iSpace.setAccessKey(scheme, host, port, token);

		
		long recordid = Long.parseLong(Helper.getRequiredParam(Helper.RECORD_ID, request));
		Record r = iSpace.findRecordForGUID(recordid);
		int pixelDepth = (int) Math.pow(2, r.getPixelDepth().getBitSize());

		

		JSONObject json = new JSONObject();
		try
		{
			json.put("Record ID", recordid);
			json.put("Slice Count", r.getSliceCount());
			json.put("Frame Count", r.getFrameCount());
			json.put("Channel Count", r.getChannelCount());
		}
		catch (JSONException e)
		{
			System.err.println("JSON (data): " + e.getMessage());
		}

		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.println(json.toString());
		out.close();
	}

}
