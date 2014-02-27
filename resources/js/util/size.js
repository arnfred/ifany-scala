define(function() {

	// Init
	var size = {};


	 /**
	  * Get size of browser window
	  */
	size.getWidth = function() {
	  var myWidth = 0;
	  if( typeof( window.innerWidth ) == 'number' ) {
		//Non-IE
		myWidth = window.innerWidth;
	  } else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
		//IE 6+ in 'standards compliant mode'
		myWidth = document.documentElement.clientWidth;
	  } else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
		//IE 4 compatible
		myWidth = document.body.clientWidth;
	  }
	  return myWidth;
	}

	size.getHeight = function() {
	  var myHeight = 0;
	  if( typeof( window.innerWidth ) == 'number' ) {
		//Non-IE
		myHeight = window.innerHeight;
	  } else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
		//IE 6+ in 'standards compliant mode'
		myHeight = document.documentElement.clientHeight;
	  } else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
		//IE 4 compatible
		myHeight = document.body.clientHeight;
	  }
	  return myHeight;
	}

	size.getOrientation = function(img) {

	}

	size.getImageSize = function(img) {
		var h = size.getHeight() + 10; // 20 is arbitrary margin
		var w = size.getWidth() + 20; // 40 is arbitrary margin as well
		var o = (img.size[0] > img.size[1]) ? "h" : "v";
		var r = img.size[1] / img.size[0];
		var maxH = 600;
		var maxW = 800;
		var result = {};

		if ((o == "v" && h > 1500 && w > 1500/r) || (o == "h" && w > 2000 && h > 2000*r)) {
			maxH = 1500;
			maxW = 2000;
		} 
		else if ((o == "v" && h > 1200 && w > 1200/r) || (o == "h" && w > 1600 && h > 1600*r)) {
			maxH = 1200;
			maxW = 1600;
		} 
		else if ((o == "v" && h > 960 && w > 960/r) || (o == "h" && w > 1280 && h > 1280*r)) {
			maxH = 980;
			maxW = 1280;
		} 
		else if ((o == "v" && h > 768 && w > 768/r) || (o == "h" && w > 1024 && h > 1024*r)) {
			maxH = 768;
			maxW = 1024;
		} 
		else if ((o == "v" && h > 600 && w > 600/r) || (o == "h" && w > 800 && h > 800*r)) {
			maxH = 600;
			maxW = 800;
		} 
		else if ((o == "v" && h > 450 && w > 450/r) || (o == "h" && w > 600 && h > 600*r)) {
			maxH = 450;
			maxW = 600;
		} 
		else {
			maxH = 300;
			maxW = 400;
		}

		// Set url
		result.url = maxW + "x" + maxH

		// Set actual size
		if (o == "v") {
			result.width = Math.floor(maxH / r);
			result.height = maxH;
		} else {
			result.width = maxW;
			result.height = Math.round(maxW * r);
		}

		return result;
	}


	// Return the size object
	return size;

});

