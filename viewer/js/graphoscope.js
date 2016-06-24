var mode = 'view';
var viewer = 0;
var canvas = 0;
var overlay = 0;
var started = false;
var lastX = 0;
var lastY = 0;
var textdata = '';
var overlays = [];
var overlayNames = [];
var editCurrentOverlay = 0;
var selectedOverlayNames = [];
var textColorFill = "#000000";

function getParameterByName(name, url) {
	if (!url) url = window.location.href;
	url = url.toLowerCase(); // This is just to avoid case sensitiveness  
	name = name.replace(/[\[\]]/g, "\\$&");

	var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
	results = regex.exec(url);
	if (!results) return null;
	if (!results[2]) return '';

	return decodeURIComponent(results[2].replace(/\+/g, " "));
}

//save fabric overlay
function saveOverlay() {
	console.log(editCurrentOverlay);
	if(editCurrentOverlay == 0){
		addOverlayButtonListener();
		alert("Canvas in view mode. Save in edit mode to make changes.");
		return;
	}
	
	var button = document.getElementById('save-overlay-button');
	overlay.fabricCanvas().isDrawingMode = false;
	alert(JSON.stringify(overlay.fabricCanvas()));
	
	var jsonStr = JSON.stringify(overlay.fabricCanvas());
	var jsonObject = JSON.parse(jsonStr);
	jsonObject["name"] = editCurrentOverlay;
	console.log(jsonObject);
	
	
	jsonStr = JSON.stringify(jsonObject["objects"]);
	console.log(jsonStr);
	var url = Util.getsaveOverlaysUrl() + "&overlayName=" + editCurrentOverlay;
	console.log(url);
	$.ajax({
		url : url,
		type: 'POST',
		dataType: 'json',
	    data : jsonStr,
	    contentType: 'application/json',
        mimeType: 'application/json',
		success : function(result) {
			console.log(result);
		}
	});
}

function MouseDown(e) {
	if (window.event && !e) e = window.event;
	if (mode == 'drawRect') return drawRectMouseDown(e);
	else if (mode == 'drawArrow') return drawArrowMouseDown(e);
	else if (mode == 'drawFree') return drawFreeMouseDown(e);
	else if (mode == 'drawCircle') return drawCircleMouseDown(e);
	else if (mode == 'drawText') return drawTextMouseDown(e);
}

function MouseMove(e) {
	if (window.event && !e) e = window.event;
	if (mode == 'drawRect') drawRectMouseMove(e);
	else if (mode == 'drawArrow') drawArrowMouseMove(e);
	else if (mode == 'drawCircle') drawCircleMouseMove(e);
	else if (mode == 'drawText') drawTextMouseMove(e);
}

function MouseUp(e) {
	if (window.event && !e) e = window.event;
	if (mode == 'drawRect') drawRectMouseUp(e);
	else if (mode == 'drawArrow') drawArrowMouseUp(e);
	else if (mode == 'drawFree') drawFreeMouseUp(e);
	else if (mode == 'drawCircle') drawCircleMouseUp(e);
	else if (mode == 'drawText') drawTextMouseUp(e);
}
function drawPolygon() {
	
	mode = "drawPolygon";
	var currentShape = 0;
	viewer.setMouseNavEnabled(false);
	viewer.outerTracker.setTracking(false);
	canvas.observe("mouse:move", function (event) {
		var pos = canvas.getPointer(event.e);
		if (mode === "edit" && currentShape) {
			var points = currentShape.get("points");
			points[points.length - 1].x = pos.x;
			points[points.length - 1].y = pos.y;
			currentShape.set({
				points: points
			});
			canvas.renderAll();
		}
	});

	canvas.observe("mouse:down", function (event) {
		var pos = canvas.getPointer(event.e);

		if (mode === "drawPolygon") {
			var polygon = new fabric.Polygon([{
				x: pos.x,
				y: pos.y
			}, {
				x: pos.x + 0.5,
				y: pos.y + 0.5
			}], {
				stroke: canvas.freeDrawingBrush.color,
				originX:'left',
				originY:'top',
				selectable:false,
				strokeWidth: 8,
				fill: null,
			});
			currentShape = polygon;
			canvas.add(currentShape);
			mode = "edit";
		} else if (mode === "edit" && currentShape && currentShape.type === "polygon") {
			var points = currentShape.get("points");
			points.push({
				x: pos.x,
				y: pos.y
			});
			currentShape.set({
				points: points
			});
			canvas.renderAll();
		}
	});
	fabric.util.addListener(window, 'dblclick', function (e) {
		if (mode === 'edit' || mode === 'drawPolygon') {
			mode = 'normal';
			currentShape.set({
				selectable: true
			});
			currentShape._calcDimensions(false);
			currentShape.setCoords();
			console.log(currentShape.get("points"));
			
			viewer.setMouseNavEnabled(true);
			//canvas.loadFromJSON({}, canvas.renderAll.bind(canvas));
			var pol = new fabric.Polygon(currentShape.get("points"), {
			                                left: currentShape.get("left"),
			                                top: currentShape.get("top"),
			                                stroke: canvas.freeDrawingBrush.color,
			                				selectable:false,
			                				strokeWidth: 8,
			                				fill: null,
			                              }
			                            );
			canvas.remove(currentShape);
			canvas.add(pol);
			
			//canvas.setActiveObject(currentShape);
			
			/*var objects = JSON.stringify(overlay.fabricCanvas());
			console.log(JSON.parse(objects));
			//canvas.loadFromJSON(objects, canvas.renderAll.bind(canvas));
			var tmp = JSON.parse(objects);
			drawOnCanvas(tmp["objects"]);
			var tmp = JSON.stringify(overlay.fabricCanvas());
			console.log(tmp);*/
			canvas.discardActiveObject();
			canvas.renderAll();

			currentShape.set('selected', true);
			setViewMode(true);
			
			
		} else {
		}
		currentShape = null;
	});

}
//draw rect
function drawRectMouseDown(e) {
	viewer.setMouseNavEnabled(false);
	viewer.outerTracker.setTracking(false);

	var mouse = canvas.getPointer(e.e);
	started = true;
	lastX = mouse.x;
	lastY = mouse.y;

	var fillColor = null;

	var square = new fabric.Rect({ 
		width: 0, 
		height: 0, 
		left: lastX, 
		top: lastY,
		fill: fillColor,
		stroke:   canvas.freeDrawingBrush.color,
		strokeWidth: canvas.freeDrawingBrush.width 
	});

	canvas.add( square ); 
	canvas.renderAll();
	canvas.setActiveObject( square ); 
}

function drawRectMouseMove(e) {
	if (!started) {
		return false;
	}
	var mouse = canvas.getPointer(e.e);

	var w = Math.abs(mouse.x - lastX),
	h = Math.abs(mouse.y - lastY);

	if (!w || !h) {
		return false;
	}

	var square = canvas.getActiveObject();

	square.set('width', w).set('height', h);
	square.set('left', lastX);
	square.set('top', lastY);

	canvas.renderAll();
}

function drawRectMouseUp(e) {
	viewer.setMouseNavEnabled(true);

	if (started) {
		started = false;
	}
	var square = canvas.getActiveObject();
	canvas.discardActiveObject();
	square.setCoords();
	canvas.renderAll();
	setViewMode(true);
}

//draw free hand
function drawFreeMouseDown(e) {
	viewer.setMouseNavEnabled(false);
	viewer.outerTracker.setTracking(false);
}

function drawFreeMouseUp(e) {
	viewer.setMouseNavEnabled(true);
}

//draw arrow
function drawArrowMouseDown( e ) {
	var mouse = canvas.getPointer(e.e);

	started = true;
	lastX = mouse.x;
	lastY = mouse.y;

	if (!e.target){
		// images
		/*fabric.Image.fromURL('images/arrow.png', function(image) {
			image.set({
				left: 130,
				top: 350,
				angle: 10
			});
			image.scale(0.3).setCoords();
			canvas.add(image);
			canvas.calcOffset();
			canvas.setActiveObject(image);

		});*/
		
		console.log("hehe" + lastX);
		test(lastX + 50, lastY + 50, lastX, lastY,canvas.freeDrawingBrush.color);
	}
	//test(lastX, lastY, lastX + 20, lastX + 20);
	viewer.setMouseNavEnabled(false);
	viewer.outerTracker.setTracking(false);
}

function drawArrowMouseMove( e ) {
	/*if(!started) {
		return false;
	}
	var mouse = canvas.getPointer(e.e); 

	var w = Math.abs(mouse.x - lastX);
	var h = Math.abs(mouse.y - lastY);

	if (!w || !h) {
		return false;
	}

	var square = canvas.getActiveObject(); 

	square.set('width', w).set('height', h);

	square.set('left', lastX);
	square.set('top', lastY);

	canvas.renderAll(); */
}

function drawArrowMouseUp( e ) {
	viewer.setMouseNavEnabled(true);
	setViewMode(true);
}

//draw circle
function drawCircleMouseDown( e ) {
	var mouse = canvas.getPointer(e.e); 
	started = true;
	lastX = mouse.x;
	lastY = mouse.y;

	var fillColor = null;

	var circle = new fabric.Ellipse({
		rx: 0,
		ry: 0, 
		width: 0, 
		height: 0, 
		left: lastX, 
		top: lastY,
		fill: fillColor,
		stroke: canvas.freeDrawingBrush.color,
		strokeWidth: canvas.freeDrawingBrush.width 
	});

	canvas.add( circle );
	canvas.setActiveObject( circle ); 
	canvas.renderAll();

	viewer.setMouseNavEnabled(false);
	viewer.outerTracker.setTracking(false);
}

function drawCircleMouseMove( e ) {
	if(!started) {
		return false;
	}
	var mouse = canvas.getPointer(e.e); 

	var w = Math.abs(mouse.x - lastX),
	h = Math.abs(mouse.y - lastY);

	if (!w || !h) {
		return false;
	}

	var circle = canvas.getActiveObject(); 

	circle.set('width', w).set('height', h);
	circle.set('rx', w/2).set('ry', h/2);

	circle.set('left', lastX);
	circle.set('top', lastY);

	canvas.renderAll(); 
}

function drawCircleMouseUp( e ) {
	viewer.setMouseNavEnabled(true);

	if(started) {
		started = false;
	}

	var circle = canvas.getActiveObject();
	circle.setCoords();
	canvas.discardActiveObject();
	canvas.renderAll();

	circle.set('selected', true);
	setViewMode(true);
}

//draw text
function drawTextMouseDown( e ) {
	viewer.setMouseNavEnabled(false);
	viewer.outerTracker.setTracking(false);

	var mouse = canvas.getPointer(e.e);
	started = true;
	lastX = mouse.x;
	lastY = mouse.y;

	var fillColor = textColorFill;

	var text = new fabric.Text(textdata, { 
		left: lastX, 
		top: lastY,
		fill: fillColor
	});

	canvas.add( text ); 
	canvas.renderAll();
	canvas.setActiveObject( text ); 
}

function drawTextMouseMove( e ) {
	if (!started) {
		return false;
	}
	var mouse = canvas.getPointer(e.e);

	var w = Math.abs(mouse.x - lastX),
	h = Math.abs(mouse.y - lastY);

	if (!w || !h) {
		return false;
	}

	var text = canvas.getActiveObject();

	text.set('width', w).set('height', h);
	text.set('left', lastX);
	text.set('top', lastY);
	text.setFontSize(h);
	canvas.renderAll();
}

function drawTextMouseUp( e ) {
	viewer.setMouseNavEnabled(true);

	if (started) {
		started = false;
	}

	var text = canvas.getActiveObject();
	//canvas.discardActiveObject();
	//text.setCoords();
	canvas.renderAll();
	setViewMode(true);
}


function init() {
	var url = window.location.href;
	Util.init();
	var recordid = Util.recordids;
	var loginurl = Util.getLoginUrl();
	doLogin(loginurl);
	relativeUrl = "/dzi/" + recordid + "/" + recordid + ".dzi";
	dziUrl = window.location.origin + relativeUrl;
	var progress = parseFloat(getProgress());
	console.log(progress);
	viewer = OpenSeadragon({
		id:            "openseadragon",
		prefixUrl:     "graphoscope/lib/openseadragon-2.1.0/images/",
		navigatorSizeRatio: 0.25,
		wrapHorizontal:     false,
		showNavigator:  true,
		navigatorId: "graphoscope-nav",
		defaultZoomLevel: 1,
		zoomInButton: "zoom-in",
		zoomOutButton: "zoom-out",
		homeButton: "home",
	});
	//viewer.open(dziUrl);
	overlay = viewer.fabricjsOverlay();

	canvas = overlay.fabricCanvas();
	
	//viewer.open(dziUrl);
	overlay.fabricCanvas().freeDrawingBrush.color='#FF0000';
	overlay.fabricCanvas().freeDrawingBrush.width=30;


	$(window).resize(function() {
		overlay.resize();
		overlay.resizecanvas();
	});
	// parent of graphoscope taking up space
	$('#graphoscope-nav').parent().css('height', '0');
	if(progress != 100 ){
	    if(progress == 0){
	       doTiling(recordid);
		   console.log("started tiling"); 
	    }
		
		/*var ctx = canvas.getContext('2d');
		console.log(canvas.width);
		ctx.rect(20,20,150,100);
		ctx.stroke();
		ctx.fillStyle = "white";
		ctx.fillRect(0, 0, canvas.width, canvas.height);*/

		var bar = new ProgressBar.Circle(container, {
			color: '#aaa',
			// This has to be the same size as the maximum width to
			// prevent clipping
			strokeWidth: 4,
			trailWidth: 1,
			easing: 'easeInOut',
			duration: 1000,
			text: {
				autoStyleContainer: false
			},
			from: { color: '#333', width: 2 },
			to: { color: '#333', width: 4 },
			// Set default step function for all animate calls
			step: function(state, circle) {
				circle.path.setAttribute('stroke', state.color);
				circle.path.setAttribute('stroke-width', state.width);

				var value = (circle.value()*100).toFixed(2);
				if (value === 0) {
					circle.setText('');
				} else {
					circle.setText(value);
				}

			}
		});
		bar.text.style.fontFamily = '"Raleway", Helvetica, sans-serif';
		bar.text.style.fontSize = '4rem';
		document.getElementById("openseadragon").style.opacity = "0.1";
        var liveProgress = 0;
        var refreshId = setInterval(function() {
        	liveProgress = parseFloat(getProgress());
        	var progressBar = parseFloat(liveProgress/100);
        	bar.animate(progressBar);
        	if (liveProgress == 100) {
        		bar.destroy();
        		document.getElementById("openseadragon").style.opacity = "1.0";
        		viewer.open(dziUrl);
        		clearInterval(refreshId);
        	}
        }, 1000); 
	}
	else if(progress == 100){
	    viewer.open(dziUrl);
        /*var  loadOverlayurl = Util.getloadOverlaysUrl(5);
    	$.ajax({
    		url : loadOverlayurl,
    		type: 'GET',
    		async:false,
    		success : function(result) {
    			//canvas.loadFromJSON(result, canvas.renderAll.bind(canvas));
    			json1 = result;
    			
    			var str = JSON.stringify(result);
    			console.log(str);
    			canvas.loadFromJSON(json1,canvas.renderAll.bind(canvas));
    		}
    	});*/
	   
        loadOverlays(recordid);
        loadTable();
        
	}
}
function loadTable(){
	checkboxes = document.getElementsByTagName("input"); 
    for (var i = 0; i < checkboxes.length; i++) {
        var checkbox = checkboxes[i];
        checkbox.onclick = function() {
            var currentRow = this.parentNode.parentNode;
            var secondColumn = currentRow.getElementsByTagName("td")[1];
            selectedOverlayNames = getSelectedOverlayNames();
            showOverlays(false);
            editCurrentOverlay = 0;
        };
    }
}
function getSelectedOverlayNames(){
	var selectedNames = [];
	for(var i=0;i<overlayNames.length;i++){
		if(document.getElementById(overlayNames[i]).checked == true) {
			selectedNames.push(document.getElementById(overlayNames[i]).value);
		}
	}
	return selectedNames;
}
function getOverlays(){
	return overlayNames;
}
function drawOnCanvas(objects){
	var result = objects.slice(0);  //make a copy 
	console.log(result);
	var arrows = [];
	for(var i =0; i < objects.length; i++){
		if(objects[i].type == "Line"){
			arrows.push(objects[i]);
			result[i] = {};
		}
	}
	for(var i =0; i < result.length; i++){
		if(result[i].type == "path"){
			var array =  result[i];
			var tmp = array.path;
			var obj = JSON.parse(tmp);
			result[i].path = obj;
		}
	}
	console.log(arrows,result);
	var array = {};
	for(var i =0; i<result.length; i++){
		console.log(i);
		if(result[i].type == null){
			result.splice(i,1);
		}
	}
	array["objects"] = result;
	 
	canvas.loadFromJSON(JSON.stringify(array),canvas.renderAll.bind(canvas));

	for(var i=0; i < arrows.length; i++){
		var array = arrows[i].points;
		console.log(array);
		var x = array[0];
		var y = array[1];
		var x1 = array[2];
		var y1 = array[3];
		var color = arrows[i].stroke;
		test(x+x1,y+y1,x-x1,y-y1,color);
	}
}
function loadOverlays(recordid){
	
	var url = Util.getVisualOverlayNamesUrl(recordid);
	var overlayNamesList = [];
	$.ajax({
		url : url,
		type: 'GET',
		async:false,
		success : function(result) {
			console.log(result);
			overlayNamesList = result;
		}
	});
	for(var i=0; i<overlayNamesList.length; i++){
		addRow(overlayNamesList[i]);
		overlayNames.push(overlayNamesList[i]);
	}
	
}
function editOverlay(name){
	editCurrentOverlay = name;
	uncheckAll();
	document.getElementById(name).checked = true;
    selectedOverlayNames = getSelectedOverlayNames();
    showOverlays(true);
}
function deleteOverlay(name){
	console.log(name);
	var recordid = Util.recordids;
	var url = Util.deleteOverlayUrl(recordid) + "&overlayName=" + name;
	$.ajax({
		url: url,
		async: false,
		success: function (result) {
			console.log(overlayNames);
			deleteRow(name);
			var index = overlayNames.indexOf(name);
			if (index > -1) {
				overlayNames.splice(index, 1);
			}
			
		}
	});
	
}
function deleteRow(name) {
	try {
		console.log(name);
		var table = document.getElementById("overlayTable");
		// delete row
		for(var i=0;i<overlayNames.length;i++){
			if( document.getElementById(overlayNames[i]).value == name ){ 
				table.deleteRow(i);
			    break;
			}
		}
	}catch(e) {
		alert(e);
	}
}
function uncheckAll(){
	for(var i=0;i<overlayNames.length;i++){
		document.getElementById(overlayNames[i]).checked = false;
	}
}
function showOverlays(selectable){
	console.log(selectedOverlayNames);
	var recordid = Util.recordids;
	var conc = {"objects":{}};
	var objects = [];
	for(var i=0; i<selectedOverlayNames.length; i++){
		console.log("i" + i);
		var name = selectedOverlayNames[i];
		var url = Util.getOverlayUrl(recordid) + "&overlayName=" + name;
		var overlayObjects = [];
		$.ajax({
			url: url,
			async: false,
			success: function (result) {
				for(var j = 0; j< result.length; j++)
					objects.push(result[j]);
				//overlayObjects.push(result);
			}
		});
	}
	drawOnCanvas(objects);
	setViewMode(selectable);
}
function checkExistence(name){
	for(var i=0; i<selectedOverlayNames.length; i++){
		if(selectedOverlayNames[i] == name){
			return true;
		}
	}
	return false;
}
function getProgress() {

	var progress = 0;
	var url = Util.getProgressUrl();
	$.ajax({
		url : url,
		async : false,
		success : function(result) {
			progress = result['progress'];
		}
	});
	return progress;
}
function doTiling(recordid) {
	console.log(recordid);
	var data = Util.getData(recordid);
	var url = Util.startTilingUrl(data);
	console.log(url);
	$.ajax({
		url : url,
		success : function(result) {
			console.log(result);
		}
	});
}
function doLogin(loginUrl) {
	$.ajax({
		url : loginUrl,
		async : false,
		success : function(result) {
			Util.token = result['token'];
			if (result['login'].toString() == 'true') {
				//alert("logged in" + Util.token);
			}
		}
	});
}
/*
function toggle_div(){
    if ($('#graphoscope-nav-wrapper').width() <= 30){
      $('#graphoscope-nav-wrapper').animate( {width: $('#graphoscope-nav-wrapper').find('.navigator').width() + 30});
    } else {
      $('#graphoscope-nav-wrapper').animate( {width: 30});
    }
}

function toggle_navigator() {
  $("#graphoscope-nav-wrapper").toggle("slide", {direction: 'right'});
}

function show_navigator() {
  $("#graphoscope-nav-wrapper").show("slide", {direction: 'right'});
}

function hide_navigator() {
  $("#graphoscope-nav-wrapper").hide("slide", {direction: 'right'});
}

function toggle_toolbar() {
  $("#tools-panel").toggle("slide");
}

function show_toolbar() {
  $("#tools-panel").show("slide");
}

function hide_toolbar() {
  $("#tools-panel").hide("slide");
}
 */

//set viewer modes

//set viewer in view mode
function setViewMode(selectable) {
	mode = 'view';
	canvas.isDrawingMode = false;
	canvas.selection = true;

	//canvas.deactivateAll();

	var objs = canvas.getObjects();
	//console.log(objs);
	for (var i = 0; i < objs.length; i++) {
		var obj = objs[i];
		obj.set('selectable', selectable);
	}

	// hide draw settings dialog
	$("#draw-settings").hide("slide");
	$("#tools-panel").css("opacity", "0.7");

	$('#delete-selected-object').prop('disabled', false);
}

//set viewer in free hand draw mode
function setDrawMode() {
	mode = 'drawFree';
	var button = document.getElementById('draw-freehand');
	overlay.fabricCanvas().isDrawingMode=true;

	// hide draw settings dialog
	$("#draw-settings").show("slide");
	$("#tools-panel").css("opacity", 1);

	$('#delete-selected-object').prop('disabled', true);

	//anno.activateSelector();
	//alert(JSON.stringify(overlay.fabricCanvas()));
}

//set viewer in draw arrow mode
function setDrawArrowMode() {
	mode = 'drawArrow';
	canvas.isDrawingMode = false;
	canvas.selection = false;

	canvas.deactivateAll();
	$("#draw-settings").show("slide");
	$('#delete-selected-object').prop('disabled', true);
}
function setDrawPolygonMode() {
	mode = 'drawPolygon';
	canvas.isDrawingMode = false;
	canvas.selection = false;

	canvas.deactivateAll();
	drawPolygon();
	$("#draw-settings").show("slide");
	$('#delete-selected-object').prop('disabled', true);
}
//set viewer in draw rect mode
function setDrawRectMode() {
	mode = 'drawRect';
	canvas.isDrawingMode = false;
	canvas.selection = false;

	canvas.deactivateAll();

	$("#draw-settings").show("slide");
	$('#delete-selected-object').prop('disabled', true);
}

//set viewer in draw circle mode
function setDrawCircleMode() {
	mode = 'drawCircle';
	canvas.isDrawingMode = false;
	canvas.selection = false;

	canvas.deactivateAll();

	$("#draw-settings").show("slide");
	$('#delete-selected-object').prop('disabled', true);
}

//remove selected object from the canvas
function removeSelectedObject() {
	var activeObject = canvas.getActiveObject(); 
	if(activeObject.type == "line"){
		canvas.remove(activeObject.circle);
		canvas.remove(activeObject.arrow);
	}
	canvas.remove(activeObject);
}

//add text to overlay
function addText() {
	$( "#textDialog" ).dialog('open');

	mode = 'drawText';
	canvas.isDrawingMode = false;
	canvas.selection = false;

	canvas.deactivateAll();

	$('#delete-selected-object').prop('disabled', true);
}

function setText(str) {
	textdata = str;
}

function addOverlayButtonListener() {
	$( "#addOverlayDialog" ).dialog('open');

	/*mode = 'drawText';
	canvas.isDrawingMode = false;
	canvas.selection = false;*/

	canvas.deactivateAll();

	//$('#delete-selected-object').prop('disabled', true);
}

function createOverlay(name) {
	
	var recordid = Util.recordids;
	var url = Util.createOverlayUrl(recordid) + "&overlayName=" + name;
	console.log(url);
    $.ajax({
		url: url,
		async: false,
		success: function (result) {
			var str1 = result.toString().trim();
			var tmp = "success";
			var str2 = tmp.toString();
			if(str1 == str2){
				console.log("successfully added");
				addRow(name);
				overlayNames.push(name);
			}
			else{
				console.log("failed");
			}
		}
	});
}
function addRow(name){
	try {
		var table = document.getElementById("overlayTable");
		// add Row
		var row = table.insertRow(table.rows.length);

		var cell1 = row.insertCell(0);
		var cell2 = row.insertCell(1);
		var cell3 = row.insertCell(2);
		var cell4 = row.insertCell(3);
        
		var checkBox = '<input id = "' + name +  '" type="checkbox" value="' +name+   '" />';
		var editButton = '<button type="button" id="draw-btn" name = "' +name+ '" onclick="editOverlay(this.name)" class="btn btn-default btn-sm toolbar-btn" data-toggle="tooltip" data-placement="top" title="Edit"><span class="glyphicon glyphicon-pencil" aria-hidden="true"></span></button>';
		var deleteButton = '<button type="button" id="draw-btn" name = "' +name+ '" onclick="deleteOverlay(this.name)" class="btn btn-default btn-sm toolbar-btn" data-toggle="tooltip" data-placement="top" title="Delete"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button>';
		
		cell1.innerHTML = checkBox;
		cell2.innerHTML = name;
		cell3.innerHTML = editButton;
		cell4.innerHTML = deleteButton;
	}catch(e) {
		alert(e);
	}
	loadTable();
}

function test(sx,sy,fx,fy,color){

    addArrowToCanvas();
    console.log("added");
    function addArrowToCanvas() {
        var line;
        var arrow;
        var circle;
        line = new fabric.Line([sx, sy, fx, fy], {
            stroke: color,
            fill : color,
            selectable: true,
            strokeWidth: '5',
            padding: 5,
            hasBorders: false,
            hasControls: false,
            originX: 'center',
            originY: 'center',
            lockScalingX: true,
            lockScalingY: true
        });

        var centerX = (line.x1 + line.x2) / 2;
        var centerY = (line.y1 + line.y2) / 2;
        var deltaX = line.left - centerX;
        var deltaY = line.top - centerY;
        
        var arrowangle = calcArrowAngle(sx, sy, fx, fy) - 90;
        console.log(arrowangle);
        arrow = new fabric.Triangle({
            left: line.get('x1') + deltaX,
            top: line.get('y1') + deltaY,
            originX: 'center',
            originY: 'center',
            hasBorders: false,
            hasControls: false,
            lockScalingX: true,
            lockScalingY: true,
            lockRotation: true,
            pointType: 'arrow_start',
            angle: arrowangle,
            width: 30,
            height: 30,
            fill: color
        });
        arrow.line = line;

        circle = new fabric.Circle({
            left: line.get('x2') + deltaX,
            top: line.get('y2') + deltaY,
            radius: 8,
            stroke: color,
            strokeWidth: 6,
            originX: 'center',
            originY: 'center',
            hasBorders: false,
            hasControls: false,
            lockScalingX: true,
            lockScalingY: true,
            lockRotation: true,
            pointType: 'arrow_end',
            fill: color
        });
        circle.line = line;

        line.customType = arrow.customType = circle.customType = 'arrow';
        line.circle = arrow.circle = circle;
        line.arrow = circle.arrow = arrow;
        
        canvas.add(line, arrow, circle);
        canvas.renderAll();
    	//canvas.setActiveObject( line, arrow, circle ); 
        //group = new fabric.Group([ circle, line, arrow ],{hasBorders: false, hasControls: false});
        //canvas.add(group);
    	
    	function moveEnd(obj) {
            var p = obj;
            var  x1, y1, x2, y2;
            console.log(obj.left,obj.top);
            if (obj.pointType === 'arrow_end') {
                obj.line.set('x2', obj.get('left'));
                obj.line.set('y2', obj.get('top'));
            } else {
                obj.line.set('x1', obj.get('left'));
                obj.line.set('y1', obj.get('top'));
            }

            obj.line._setWidthHeight();

            x1 = obj.line.get('x1');
            y1 = obj.line.get('y1');
            x2 = obj.line.get('x2');
            y2 = obj.line.get('y2');

            var angle = calcArrowAngle(x1, y1, x2, y2);

            if (obj.pointType === 'arrow_end') {
                obj.arrow.set('angle', angle - 90);
            } else {
                obj.set('angle', angle - 90);
            }

            obj.line.setCoords();
            canvas.renderAll();
        }
    	

        function moveLine() {
            var oldCenterX = (line.x1 + line.x2) / 2,
                oldCenterY = (line.y1 + line.y2) / 2,
                deltaX = line.left - oldCenterX,
                deltaY = line.top - oldCenterY;

            line.arrow.set({
                'left': line.x1 + deltaX,
                'top': line.y1 + deltaY
            }).setCoords();

            line.circle.set({
                'left': line.x2 + deltaX,
                'top': line.y2 + deltaY
            }).setCoords();

            line.set({
                'x1': line.x1 + deltaX,
                'y1': line.y1 + deltaY,
                'x2': line.x2 + deltaX,
                'y2': line.y2 + deltaY
            });

            line.set({
                'left': (line.x1 + line.x2) / 2,
                'top': (line.y1 + line.y2) / 2
            });
        }

        arrow.on('moving', function () {
            moveEnd(arrow);
        });

        circle.on('moving', function () {
            moveEnd(circle);
        });

        line.on('moving', function () {
            moveLine();
        });
    
    }
    function calcArrowAngle(x1, y1, x2, y2) {
        var angle = 0,
            x, y;

        x = (x2 - x1);
        y = (y2 - y1);

        if (x === 0) {
            angle = (y === 0) ? 0 : (y > 0) ? Math.PI / 2 : Math.PI * 3 / 2;
        } else if (y === 0) {
            angle = (x > 0) ? 0 : Math.PI;
        } else {
            angle = (x < 0) ? Math.atan(y / x) + Math.PI : (y < 0) ? Math.atan(y / x) + (2 * Math.PI) : Math.atan(y / x);
        }

        return (angle * 180 / Math.PI);
    }
    
}

//on document ready
$(function(){
	$("#line-width").on( "slidechange", function( event, ui ) {
		console.log(ui.value);
		overlay.fabricCanvas().freeDrawingBrush.width=ui.value;
	});

	$('#red-btn').on('click', function () {
		overlay.fabricCanvas().freeDrawingBrush.color='#FF0000';
	});

	$('#orange-btn').on('click', function () {
		overlay.fabricCanvas().freeDrawingBrush.color='#ffa500';
	});

	$('#blue-btn').on('click', function () {
		overlay.fabricCanvas().freeDrawingBrush.color='#0000FF';
	});

	$('#green-btn').on('click', function () {
		overlay.fabricCanvas().freeDrawingBrush.color='#008000';
	});

	$('#black-btn').on('click', function () {
		overlay.fabricCanvas().freeDrawingBrush.color='#000000';
	});

	$('#white-btn').on('click', function () {
		overlay.fabricCanvas().freeDrawingBrush.color='#FFFFFF';
	});

	$(".basic").spectrum({
	    color: "#000",
	    change: function(color) {
	        console.log(color.toHexString());
	        textColorFill = color.toHexString();
	    }
	});
	init();

	canvas.isDrawingMode = false;
	fabric.isTouchSupported = false;

	canvas.on('mouse:down', function (e) {
		MouseDown(e);
	});

	canvas.on('mouse:move', function (e) {
		MouseMove(e);
	});

	canvas.on('mouse:up', function (e) {
		MouseUp(e);
	});

	$( "#textDialog" ).dialog({
		autoOpen: false,
		height: 220,
		width: 350,
		modal: true,
		buttons: {
			"Add": function() {
				setText($("#textDialogTextArea").val());
				$(this).dialog("close");
			}
		},
		Cancel: function() {
			$(this).dialog("close");
		},
		close: function() {
			$("#textDialogTextArea").val('');
		}
	});
	$( "#addOverlayDialog" ).dialog({
		autoOpen: false,
		height: 220,
		width: 350,
		modal: true,
		buttons: {
			"Add": function() {
				createOverlay($("#addOverlayDialogTextArea").val());
				$(this).dialog("close");
			}
		},
		Cancel: function() {
			$(this).dialog("close");
		},
		close: function() {
			$("#addOverlayDialogTextArea").val('');
		}
	});

});
