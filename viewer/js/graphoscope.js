var mode = 'view';
var viewer = 0;
var canvas = 0;
var overlay = 0;
var started = false;
var lastX = 0;
var lastY = 0;

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
  var button = document.getElementById('save-overlay-button');
  overlay.fabricCanvas().isDrawingMode = false;
  alert(JSON.stringify(overlay.fabricCanvas()));
}

function MouseDown(e) {
    if (window.event && !e) e = window.event;

    if (mode == 'drawRect') return drawRectMouseDown(e);
    else if (mode == 'drawArrow') return drawArrowMouseDown(e);
    else if (mode == 'drawFree') return drawFreeMouseDown(e);
    else if (mode == 'drawCircle') return drawCircleMouseDown(e);
}

function MouseMove(e) {
    if (window.event && !e) e = window.event;

    if (mode == 'drawRect') drawRectMouseMove(e);
    else if (mode == 'drawArrow') drawArrowMouseMove(e);
    else if (mode == 'drawCircle') drawCircleMouseMove(e);
}

function MouseUp(e) {
    if (window.event && !e) e = window.event;

    if (mode == 'drawRect') drawRectMouseUp(e);
    else if (mode == 'drawArrow') drawArrowMouseUp(e);
    else if (mode == 'drawFree') drawFreeMouseUp(e);
    else if (mode == 'drawCircle') drawCircleMouseUp(e);
}

// draw rect
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
}

// draw free hand
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
      fabric.Image.fromURL('graphoscope/images/arrow.png', function(image) {
        image.set({
          left: 130,
          top: 350,
          angle: 10
        });
        image.scale(0.3).setCoords();
        canvas.add(image);
        canvas.calcOffset();
        canvas.setActiveObject(image);

      });
    }
    viewer.setMouseNavEnabled(false);
    viewer.outerTracker.setTracking(false);
}

function drawArrowMouseMove( e ) {
    if(!started) {
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

    canvas.renderAll(); 
}

function drawArrowMouseUp( e ) {
    if(started) {
        started = false;
    }
    var tri = canvas.getActiveObject();
    canvas.discardActiveObject();
    tri.setCoords();
    canvas.renderAll();

    tri.set('selected', true );

    viewer.setMouseNavEnabled(true);
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
        perPixelTargetFind: true, 
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
}


function init() {
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
    tileSources:  getParameterByName("source") 
  });

  overlay = viewer.fabricjsOverlay();

  canvas = overlay.fabricCanvas();
  overlay.fabricCanvas().freeDrawingBrush.color='red';
  overlay.fabricCanvas().freeDrawingBrush.width=30;
  
  
  $(window).resize(function() {
      overlay.resize();
      overlay.resizecanvas();
  });

  // parent of graphoscope taking up space
  $('#graphoscope-nav').parent().css('height', '0');
}

function toggle_navigator() {
  $("#graphoscope-nav").toggle();
}

// set viewer modes

// set viewer in view mode
function setViewMode() {
    mode = 'view';
    canvas.isDrawingMode = false;
    canvas.selection = true;

    canvas.deactivateAll();

    var objs = canvas.getObjects();
    for (var i = 0; i < objs.length; i++) {
        var obj = objs[i];
        obj.set('selectable', true);
    }

    // hide draw settings dialog
    $("#draw-settings").hide();
    $("#draw-btn").show();
    $("#delete-selected-object").show();
    $("#view-mode").hide();
    $("#tools-panel").css("opacity", "0.7");
}

// set viewer in free hand draw mode
function setDrawMode() {
  mode = 'drawFree';
  var button = document.getElementById('draw-freehand');
  overlay.fabricCanvas().isDrawingMode=true;

  // hide draw settings dialog
  $("#draw-settings").show();
  $("#draw-btn").hide();
  $("#delete-selected-object").hide();
  $("#view-mode").show();
  $("#tools-panel").css("opacity", 1);

  //anno.activateSelector();
  //alert(JSON.stringify(overlay.fabricCanvas()));
}

// set viewer in draw arrow mode
function setDrawArrowMode() {
    mode = 'drawArrow';
    canvas.isDrawingMode = false;
    canvas.selection = false;

    canvas.deactivateAll();
}

// set viewer in draw rect mode
function setDrawRectMode() {
    mode = 'drawRect';
    canvas.isDrawingMode = false;
    canvas.selection = false;

    canvas.deactivateAll();
}

// set viewer in draw circle mode
function setDrawCircleMode() {
    mode = 'drawCircle';
    canvas.isDrawingMode = false;
    canvas.selection = false;

    canvas.deactivateAll();
}

// remove selected object from the canvas
function removeSelectedObject() {
  var activeObject = canvas.getActiveObject(); 
  canvas.remove(activeObject);
}

// on document ready
$(function(){
  $("#line-width").on( "slidechange", function( event, ui ) {
    overlay.fabricCanvas().freeDrawingBrush.width=ui.value;
  });

  $('#red-btn').on('click', function () {
    overlay.fabricCanvas().freeDrawingBrush.color='red';
  });

  $('#orange-btn').on('click', function () {
    overlay.fabricCanvas().freeDrawingBrush.color='#ffa500';
  });

  $('#blue-btn').on('click', function () {
    overlay.fabricCanvas().freeDrawingBrush.color='blue';
  });

  $('#green-btn').on('click', function () {
    overlay.fabricCanvas().freeDrawingBrush.color='green';
  });

  $('#black-btn').on('click', function () {
    overlay.fabricCanvas().freeDrawingBrush.color='black';
  });

  $('#white-btn').on('click', function () {
    overlay.fabricCanvas().freeDrawingBrush.color='white';
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

});