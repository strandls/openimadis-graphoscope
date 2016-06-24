// Define Utilities
var Util = {
		serverAddr: window.location.origin + "/graphviewer/ViewerServlet/",
		initCenter: [0, 0],

		initZoom: function (container) {
			return Math.abs(Math.floor((Math.log(Math.min($("#" + container).width(), $("#" + container).height())) / Math.log(2))) - 8);
		},
		
		maxZoom: function (data) {
			return Math.max((Math.ceil((Math.log(data.imageDim) - Math.log(256)) / Math.log(2)) + 2), 5);
		},

		init: function () {
			var params = {};
			var tokens;
			var re = /[?&]?([^=]+)=([^&]*)/g;
			while (tokens = re.exec(location.search.split("+").join(" "))) {
				params[decodeURIComponent(tokens[1])] = decodeURIComponent(tokens[2]);
			}
			this.scheme = params['scheme'];
			this.host = params['host'];
			this.port = params['port'];
			this.authcode = params['authcode'];
			this.recordids = JSON.parse(params['guids']);
		},

		// Fetch all Record Data
		getData: function (recordid) {
			var data = {};
			data.recordid = recordid;
			data.sliceNumber = 0;
			data.frameNumber = 0;
			data.isGrayScale = 0;
			data.isZStacked = 0;
			data.channels = 1;
			data.imageWidth = 0;
			data.imageHeight= 0;
			var url_tmp = this.serverAddr + "getRecordData" +
			"?r=" + data.recordid +
			"&t=" + this.token+
			"&host=" + this.host +
			"&port=" + this.port+
			"&scheme=" + this.scheme;
			console.log(url_tmp);
			// Get the Record data
			$.ajax({
				url: this.serverAddr + "getRecordData" +
						"?r=" + data.recordid +
						"&t=" + this.token+
						"&host=" + this.host +
						"&port=" + this.port+
						"&scheme=" + this.scheme,
				async: false,
				success: function (result) {
					data.frameCount = result["Frame Count"];
					data.sliceCount = result["Slice Count"];
					data.channels = result["Channel Count"];
				}
			});
			return data;
		},
		startTilingUrl: function (data) {
			return this.serverAddr + "startTiling" +
					"?r=" + this.recordids +
					"&sn=" + data.sliceNumber +
					"&fn=" + data.frameNumber +
					"&cn=" + data.channels +
					"&gs=" + data.isGrayScale +
					"&zs=" + data.isZStacked +
					"&t=" + this.token+
					"&host=" + this.host +
					"&port=" + this.port+
					"&scheme=" + this.scheme ;
		},
		getProgressUrl: function () {
			return this.serverAddr + "getProgress" +
					"?r=" + this.recordids +
					"&t=" + this.token+
					"&host=" + this.host +
					"&port=" + this.port+
					"&scheme=" + this.scheme ;
		},
		getsaveOverlaysUrl: function () {
			return this.serverAddr + "saveOverlays" +
					"?r=" + this.recordids +
					"&t=" + this.token+
					"&host=" + this.host +
					"&port=" + this.port+
					"&scheme=" + this.scheme ;
		},
		getloadOverlaysUrl: function (name) {
			return this.serverAddr + "loadOverlays" +
					"?r=" + this.recordids +
					"&t=" + this.token+
					"&host=" + this.host +
					"&port=" + this.port+
					"&name=" + name+
					"&scheme=" + this.scheme ;
		},
		getTileUrl: function (data) {
			return this.serverAddr + "getTile" +
					"?r=" + data.recordid +
					"&sn=" + data.sliceNumber +
					"&fn=" + data.frameNumber +
					"&cn=" + data.channels +
					"&gs=" + data.isGrayScale +
					"&zs=" + data.isZStacked +
					"&iw=" +data.imageWidth+
					"&ih=" +data.imageHeight+
					"&t=" + this.token+
					"&host=" + this.host +
					"&port=" + this.port+
					"&scheme=" + this.scheme ;
		},

		getThumbnailUrl: function (recordid) {
			return this.serverAddr + "getThumbnail" +
					"?r=" + recordid +
					"&t=" + this.token+
					"&host=" + this.host +
					"&port=" + this.port+
					"&scheme=" + this.scheme;
		},

		getLoginUrl: function () {
			return this.serverAddr + "doLogin" +
					"?scheme=" + this.scheme +
					"&host=" + this.host +
					"&port=" + this.port +
					"&authcode=" + this.authcode;
		},
		
		getOverlayUrl: function (recordid) {
			return this.serverAddr + "getOverlays" +
					"?scheme=" + this.scheme +
					"&host=" + this.host +
					"&port=" + this.port +
					"&t=" + this.token +
					"&r="+recordid;
		},
		
		createOverlayUrl: function (recordid) {
			return this.serverAddr + "createOverlay" +
					"?scheme=" + this.scheme +
					"&host=" + this.host +
					"&port=" + this.port +
					"&t=" + this.token +
					"&r="+recordid;
		},
		
		deleteOverlayUrl: function (recordid) {
			return this.serverAddr + "deleteOverlay" +
					"?scheme=" + this.scheme +
					"&host=" + this.host +
					"&port=" + this.port +
					"&t=" + this.token +
					"&r="+recordid;
		},
		
		searchOverlayUrl: function (recordid) {
			return this.serverAddr + "searchOverlay" +
					"?scheme=" + this.scheme +
					"&host=" + this.host +
					"&port=" + this.port +
					"&t=" + this.token +
					"&r="+recordid;
		},
		
		saveOverlayUrl: function (recordid) {
			return this.serverAddr + "saveOverlays" +
					"?scheme=" + this.scheme +
					"&host=" + this.host +
					"&port=" + this.port +
					"&t=" + this.token +
					"&r="+recordid;
		},
		
		getVisualOverlayNamesUrl: function (recordid) {
			return this.serverAddr + "getVisualOverlayNames" +
					"?scheme=" + this.scheme +
					"&host=" + this.host +
					"&port=" + this.port +
					"&t=" + this.token +
					"&r="+recordid;
		}
};