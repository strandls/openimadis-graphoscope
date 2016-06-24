package com.strandgenomics.imaging.graphoscope;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.strandgenomics.imaging.graphoscope.VisualObjectsTransformer.KineticTransformer;
import com.strandgenomics.imaging.graphoscope.VisualObjectsTransformer.VisualObjectTransformer;
import com.strandgenomics.imaging.graphoscope.VisualObjectsTransformer.VisualObjectsFactory;
import com.strandgenomics.imaging.graphoscope.tiling.MultiThreadTiler;
import com.strandgenomics.imaging.graphoscope.tiling.RecordParameters;
import com.strandgenomics.imaging.graphoscope.tiling.SimpleTiler;
import com.strandgenomics.imaging.iclient.AuthenticationException;
import com.strandgenomics.imaging.iclient.ImageSpaceObject;
import com.strandgenomics.imaging.iclient.ImageSpaceSystem;
import com.strandgenomics.imaging.iclient.Record;
import com.strandgenomics.imaging.iclient.impl.ws.ispace.Area;
import com.strandgenomics.imaging.icore.Channel;
import com.strandgenomics.imaging.icore.Dimension;
import com.strandgenomics.imaging.icore.IVisualOverlay;
import com.strandgenomics.imaging.icore.VODimension;
import com.strandgenomics.imaging.icore.VisualContrast;
import com.strandgenomics.imaging.icore.image.Histogram;
import com.strandgenomics.imaging.icore.vo.VisualObject;
import com.strandgenomics.imaging.icore.vo.VisualObjectType;


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
		Record record = ispace.findRecordForGUID(guid);
		if(record.getImageHeight() < 4096 && record.getImageWidth() < 4096){
			SimpleTiler s = new SimpleTiler(storageRoot, guid, ispace, recordParams);
			s.doTiling(0, 0, record.getImageWidth(), record.getImageHeight());
		}
		else{
			MultiThreadTiler m = new MultiThreadTiler(guid, ispace,storageRoot,recordParams);
			m.executeTiling();
		}
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
	public void getProgress(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		long recordid = Long.parseLong(Helper.getRequiredParam(Helper.RECORD_ID, request));
		
		String token = Helper.getRequiredParam(Helper.TOKEN, request);
		String host = Helper.getRequiredParam(Helper.HOST, request);
		int port = Integer.parseInt(Helper.getOptionalParam(Helper.PORT, request, "80"));
		boolean scheme = Helper.getOptionalParam(Helper.SCHEME, request, "http").compareTo("https") == 0;
		
		ImageSpaceSystem iSpace = null;
		iSpace = ImageSpaceObject.getImageSpace();
		iSpace.setAccessKey(scheme, host, port, token);
		Record r = iSpace.findRecordForGUID(recordid);
		
		
		int width = r.getImageWidth();
		int height = r.getImageHeight();
		int levels = (int) (Math.log(Math.max(height, width))/Math.log(2)) + 1;
		int totalFiles = getFilesCount(width, height);
		
		File recordDir = new File(storageRoot,String.valueOf(recordid));
		File recordFilesDir = new File(recordDir,String.valueOf(recordid) + "_files");
		int tiledCount = 0;
		float progress = 0;
		DecimalFormat df = new DecimalFormat("#.##");
		if(recordDir.exists() && recordFilesDir.exists()){
			for(int i = 0; i <= levels; i++){
				File subLevel = new File(recordFilesDir,String.valueOf(i));
				if(subLevel.exists()){
					int count = subLevel.listFiles().length;
					tiledCount += count;
					//System.out.println("count at " + i + " " + count  );
				}
			}
			//System.out.println("total"+ totalFiles);
			//System.out.println("total done"+ tiledCount);
			progress = (float) tiledCount/totalFiles;
			progress *= 100;
			
			System.out.println("progress " + progress);
		}
		
		JSONObject json = new JSONObject();
		try
		{
			json.put("progress", df.format(progress));
		}
		catch (JSONException e)
		{
			System.err.println("JSON (data): " + e.getMessage());
		}
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.println(json.toString());
		out.close();
		/*JSONObject x = getDefaultContrastSettings(iSpace, recordid);
		int[] contrast = getContrastArray(x);
		System.out.println(contrast.length + " contrasts length");*/
	}
	private int getFilesCount(int width,int height){
		int levels = (int) (Math.log(Math.max(height, width))/Math.log(2)) + 1;
		int count = 0;
		int w =width, h =height;
		for(int i = levels; i >= 0; i--){
			int tilesX = (int) Math.ceil((float) w/256);
			int tilesY = (int) Math.ceil((float) h/256);
			int tiles = tilesX * tilesY;
			//System.out.println("tiles at level : " + i + "-" +  tiles);
			count += tiles;
			w =  (int) Math.ceil((float) w/2);
			h =  (int) Math.ceil((float) h/2);
		}
		return count;
	}
	private JSONObject getDefaultContrastSettings(ImageSpaceSystem iSpace, long recordid)
	{
		Record r = iSpace.findRecordForGUID(recordid);

		List<Channel> channels = iSpace.getRecordChannels(recordid);
		int contrastTileWidth = r.getImageWidth() > 4096 ? 4096 : r.getImageWidth();
		int contrastTileHeight = r.getImageHeight() > 4096 ? 4096 : r.getImageHeight();

		JSONObject json = new JSONObject();
		for (int channel = 0; channel < channels.size(); channel++)
		{
			VisualContrast contrast = channels.get(channel).getContrast(false);
			Histogram his = iSpace.getIntensityDistibutionForTile(recordid, new Dimension(0, 0, channel, 0), new Rectangle(0, 0, contrastTileWidth,
					contrastTileHeight));

			int contrastMin = contrast == null ? his.getMin() : contrast.getMinIntensity();
			int contrastMax = contrast == null ? his.getMax() : contrast.getMaxIntensity();

			JSONArray minmax = new JSONArray();
			minmax.put(contrastMin);
			minmax.put(contrastMax);

			try
			{
				json.put(channel + "", minmax);
			}
			catch (JSONException e)
			{
				System.err.println("JSON (data): " + e.getMessage());
			}
		}

		return json;
	}
	private int[] getContrastArray(JSONObject json)
	{
		int n = json.length();
		int[] contrastArray = new int[n * 2];
		for (int i = 0; i < n; i++)
		{
			try
			{
				JSONArray minmax = (JSONArray) json.get(i + "");
				contrastArray[2 * i] = (Integer) minmax.get(0);
				contrastArray[(2 * i) + 1] = (Integer) minmax.get(1);
			}
			catch (JSONException e)
			{
				System.err.println("JSON (data): " + e.getMessage());
			}
		}
		return contrastArray;
	}
	public void saveOverlays(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException
	{
		long recordid = Long.parseLong(Helper.getRequiredParam(Helper.RECORD_ID, request));
		//String name = request.getParameter("name");
		String token = Helper.getRequiredParam(Helper.TOKEN, request);
		String host = Helper.getRequiredParam(Helper.HOST, request);
		int port = Integer.parseInt(Helper.getOptionalParam(Helper.PORT, request, "80"));
		boolean scheme = Helper.getOptionalParam(Helper.SCHEME, request, "http").compareTo("https") == 0;
		String overlayname = request.getParameter("overlayName");
		System.out.println("createOVerlay " + recordid+token+host+port+scheme);
		
		ImageSpaceSystem iSpace = null;
		iSpace = ImageSpaceObject.getImageSpace();
		iSpace.setAccessKey(scheme, host, port, token);
		Record r = iSpace.findRecordForGUID(recordid);
		VODimension coordinate = new VODimension(0,0,0);
		Area a = new Area(r.getImageHeight(), r.getImageWidth(), 0, 0);
		
		Collection<VisualObject> visualObjectsinArea = iSpace.findVisualObjects(recordid, coordinate, overlayname, a);
		
		if(visualObjectsinArea!=null){
			iSpace.deleteVisualObjects(recordid, visualObjectsinArea, overlayname, coordinate);
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        /*String json = "";
        if(br != null){
            json = br.readLine();
        }
        System.out.println(json);*/
        String visualObjectsString = "";
        if(br != null){
        	visualObjectsString = br.readLine();
        }
        System.out.println(visualObjectsString);
        List<VisualObject> visualObjects = new ArrayList<VisualObject>();
		
		List<Map<String, Object>> overlays = new ObjectMapper().readValue(visualObjectsString, List.class);
		System.out.println( overlays.size() + " is the size. ");
		if(visualObjectsString!=null){
			VisualObjectTransformer instance = VisualObjectsFactory.getVisualObjectTransformer("kinetic");
			KineticTransformer t = new KineticTransformer();	
            for (Map<String, Object> overlay : overlays)
            { 
            	VisualObjectType type = t.getType(overlay);
            	if(type!=null){
            		System.out.println(type.toString());
                	VisualObject obj = instance.decode(overlay);
                	//obj.setZoomLevel((Integer)overlay.get("zoom_level"));
                	//System.out.println("zoom_level:"+obj.getZoomLevel());
                	if(obj!=null){
                		System.out.println("type:"+obj.getType());
                        visualObjects.add(obj);
                	}
                	
            	}
            	
            }
		}
		System.out.println(visualObjects.size() + "suze");
		iSpace.addVisualObjects(recordid, visualObjects, overlayname, coordinate);
		
		System.out.println("DONE");
		/*StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) { report an error }
		System.out.println(jb.toString());
		try {
			JSONObject jsonObject = HTTP.toJSONObject(json);
			System.out.println(jsonObject.toString());
		} catch (Exception e) {
			// crash and burn
			//throw new IOException("Error parsing JSON request string");
			e.printStackTrace();
		}
		*/
        /*JSONObject jObj = new JSONObject(json); 
        Iterator it = jObj.keys(); //gets all the keys

        while(it.hasNext())
        {
            String key = (String) it.next(); // get key
            Object o = jObj.get(key); // get value
            System.out.println(key + " : " +  o); // print the key and value
        }
        File recordDir = new File(storageRoot,String.valueOf(recordid));
        FileWriter file = new FileWriter(new File(recordDir,"overlay.json"));
        file.write(jObj.toString());
        file.close();*/
	}
	public void getVisualOverlayNames (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException{
		long recordid = Long.parseLong(Helper.getRequiredParam(Helper.RECORD_ID, request));
		
		String token = Helper.getRequiredParam(Helper.TOKEN, request);
		String host = Helper.getRequiredParam(Helper.HOST, request);
		int port = Integer.parseInt(Helper.getOptionalParam(Helper.PORT, request, "80"));
		boolean scheme = Helper.getOptionalParam(Helper.SCHEME, request, "http").compareTo("https") == 0;		
		
		ImageSpaceSystem iSpace = null;
		iSpace = ImageSpaceObject.getImageSpace();
		iSpace.setAccessKey(scheme, host, port, token);
		
		VODimension coordinate = new VODimension(0,0,0);
		
		Collection<IVisualOverlay> visualOverlays = iSpace.getVisualOverlays(recordid, coordinate);
		
		List<String> visualOverlaysNames = new ArrayList<String>();
		
		if(visualOverlays!=null){
			for(IVisualOverlay ivo : visualOverlays){
				visualOverlaysNames.add(ivo.getName());
			}
		}
		
        Gson gson = new Gson();
		String json = gson.toJson(visualOverlaysNames);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
	}
	/**
	 * delete overlay in record
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void deleteOverlay (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		System.out.println("deleteOverlay");
		long recordid = Long.parseLong(Helper.getRequiredParam(Helper.RECORD_ID, request));
		
		String token = Helper.getRequiredParam(Helper.TOKEN, request);
		String host = Helper.getRequiredParam(Helper.HOST, request);
		int port = Integer.parseInt(Helper.getOptionalParam(Helper.PORT, request, "80"));
		boolean scheme = Helper.getOptionalParam(Helper.SCHEME, request, "http").compareTo("https") == 0;
		
		String overlayname = request.getParameter("overlayName");

		ImageSpaceSystem iSpace = null;
		iSpace = ImageSpaceObject.getImageSpace();
		iSpace.setAccessKey(scheme, host, port, token);
		
		iSpace.deleteVisualOverlays(recordid,0, overlayname);
	}
	public void getOverlays (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException{
		long recordid = Long.parseLong(Helper.getRequiredParam(Helper.RECORD_ID, request));
		String name = request.getParameter("name");
		String token = Helper.getRequiredParam(Helper.TOKEN, request);
		String host = Helper.getRequiredParam(Helper.HOST, request);
		int port = Integer.parseInt(Helper.getOptionalParam(Helper.PORT, request, "80"));
		boolean scheme = Helper.getOptionalParam(Helper.SCHEME, request, "http").compareTo("https") == 0;
		System.out.println("getOVerlay " + recordid+token+host+port+scheme);
		
		String overlayname = request.getParameter("overlayName");

		ImageSpaceSystem iSpace = null;
		iSpace = ImageSpaceObject.getImageSpace();
		iSpace.setAccessKey(scheme, host, port, token);
		
		VODimension coordinate = new VODimension(0,0,0);
		Record r = iSpace.findRecordForGUID(recordid);
		Area a = new Area(r.getImageHeight(), r.getImageWidth(), 0, 0);
		
		
		Collection<VisualObject> visualObjects = iSpace.findVisualObjects(recordid, coordinate, overlayname, a);
		
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		
        if (visualObjects != null) {
        	VisualObjectTransformer instance = VisualObjectsFactory.getVisualObjectTransformer("kinetic");
            for (VisualObject vo : visualObjects){
            	Map<String, Object> DataObject = (Map<String, Object>)instance.encode(vo);
            	//DataObject.put("zoom_level", vo.getZoomLevel());
            	ret.add(DataObject);
            }
        }
        
       // System.out.println("No of object="+visualObjects.size());
        
        Gson gson = new Gson();
		String json = gson.toJson(ret);
		System.out.println(json);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
	}
	public void loadOverlays(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException
	{
		long recordid = Long.parseLong(Helper.getRequiredParam(Helper.RECORD_ID, request));
		String name = request.getParameter("name");
		String token = Helper.getRequiredParam(Helper.TOKEN, request);
		String host = Helper.getRequiredParam(Helper.HOST, request);
		int port = Integer.parseInt(Helper.getOptionalParam(Helper.PORT, request, "80"));
		boolean scheme = Helper.getOptionalParam(Helper.SCHEME, request, "http").compareTo("https") == 0;
		System.out.println("getOVerlay " + recordid+token+host+port+scheme);
		
		File recordDir = new File(storageRoot,String.valueOf(recordid));
		File overlayJSON = new File(recordDir, name);
		JSONParser parser = new JSONParser();
		org.json.simple.JSONObject jsonObject = null;
		try {

			Object obj = parser.parse(new FileReader(overlayJSON));

			jsonObject = (org.json.simple.JSONObject) obj;

			String type = (String) jsonObject.get("type");
			System.out.println(type);

			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.println(jsonObject.toString());
		out.close();
	}
	public void createOverlay (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		System.out.println("createOverlay");
		long recordid = Long.parseLong(Helper.getRequiredParam(Helper.RECORD_ID, request));
		
		String token = Helper.getRequiredParam(Helper.TOKEN, request);
		String host = Helper.getRequiredParam(Helper.HOST, request);
		int port = Integer.parseInt(Helper.getOptionalParam(Helper.PORT, request, "80"));
		boolean scheme = Helper.getOptionalParam(Helper.SCHEME, request, "http").compareTo("https") == 0;
		
		String overlayname = request.getParameter("overlayName");

		ImageSpaceSystem iSpace = null;
		iSpace = ImageSpaceObject.getImageSpace();
		iSpace.setAccessKey(scheme, host, port, token);
		
		iSpace.createVisualOverlays(recordid,0, overlayname);
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		out.println("success");
		out.close();
	}
}
