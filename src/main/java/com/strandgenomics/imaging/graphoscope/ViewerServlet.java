package com.strandgenomics.imaging.graphoscope;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.strandgenomics.imaging.iclient.ImageSpaceObject;
import com.strandgenomics.imaging.iclient.ImageSpaceSystem;
import com.strandgenomics.imaging.tileviewer.Helper;


@WebServlet("/ViewerServlet/*")
public class ViewerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Viewer viewer;
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, 
	     HttpServletResponse response)
	 */
	/*public ViewerServlet() {
		super();
	}*/
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		viewer = new Viewer(config.getServletContext());
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
	    throws ServletException, IOException {
		callMethod(request, response);
		//response.getWriter().write("Hello, world!");
	}	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, 
	     HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
	    throws ServletException, IOException {
		// TODO Auto-generated method stub
		callMethod(request, response);
	} 
	private void callMethod(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String remainingURI = parseRequest(request);
		System.out.println("remainingURI ViewerServelet.java "+ remainingURI);
		if (remainingURI == null || remainingURI.length() == 0) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} else {
			String methodName = remainingURI.split("/")[0].trim();
			try {
				Method method = viewer.getClass().getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
				System.out.println("method ViewerServelet.java "+ methodName);
				method.invoke(viewer, request, response);
			} catch (SecurityException e) {
				throw new ServletException(e);
			} catch (NoSuchMethodException e) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (IllegalArgumentException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} catch (IllegalAccessException e) {
				throw new ServletException(e);
			} catch (InvocationTargetException e) {
				throw new ServletException(e.getCause());
			}
		}
	}
	
	/**
	 * Parse the request to get the part relevant for multiplexing
	 * 
	 * @param request
	 * @return
	 */
	private static String parseRequest(HttpServletRequest request) {
		String servletPath = request.getContextPath() + request.getServletPath();
		String requestURI = request.getRequestURI();
		String remainingURI = "";
		// Handle the case where there is no leading slash
		if (servletPath.length() < requestURI.length()) {
			remainingURI = requestURI.substring(servletPath.length() + 1, requestURI.length());
		}
		return remainingURI;
	}
}
