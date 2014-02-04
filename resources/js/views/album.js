define(["jquery", "radio", "util/size", "util/cache"],
	function($, radio, size, cache) {

	//////////////////////////////////////////////
	//											//
	//				  Interface					//
	//											//
	//////////////////////////////////////////////
	var album = {};



	//////////////////////////////////////////////
	//											//
	//				   Events					//
	//											//
	//////////////////////////////////////////////

	album.events = function() {

		// Window resize event
		radio("window:resize").subscribe(resizeOverlay);

		// Overlay change
		radio("overlay:set").subscribe(overlayChange);
	
		// Overlay close
		radio("overlay:close").subscribe(overlayClose);

	}



	//////////////////////////////////////////////
	//											//
	//					Init					//
	//											//
	//////////////////////////////////////////////

	album.init = function() {

		// Toggle events 
		album.events();
	}


	//////////////////////////////////////////////
	//											//
	//			  Private Functions				//
	//											//
	//////////////////////////////////////////////

	// When we change image
	var overlayChange = function(img, hasPrev, hasNext) {
		navArrowUpdate(hasPrev, hasNext);
		overlayUpdate(img);
		$("#overlay").fadeIn();
		$("body").css("overflow-y", "hidden");
		resizeOverlay();
	}


	var overlayClose = function() {
		$("body").css("overflow-y", "auto");
		$("#overlay").fadeOut();
	}


	var navArrowUpdate = function(hasPrev, hasNext) {
		// Make sure we hide or show previous arrow
		if (hasPrev) $("#overlay-prev").fadeIn(300)
		else $("#overlay-prev").hide();

		// Make sure we hide or show next arrow
		if (hasNext) $("#overlay-next").fadeIn(300)
		else $("#overlay-next").hide();
	}


	var overlayUpdate = function(img) {
		var dom_img = cache.load(img);
		$("#overlay-img img").remove();
		$("#overlay-img div").prepend(dom_img);
		$("#caption").html(img.description);
		$("#overlay-img img").attr("alt",img.description);
	}


	var resizeOverlay = function() {
		window.scrollTo(0, 1);
		var captionHeight = $("#caption").height()
		var ratio = $("div#overlay-img img").width() / $("div#overlay-img").height();
		var height = size.getHeight() - captionHeight - 4;
		$("div#overlay-img div").css("height", height + "px");
		var w = $("#overlay-img img").width();
		var p = ($("#overlay-img div").width() - w) / 2.0;
		//$("#caption").css("padding", "0 " + Math.floor(p) + "px");
		//$("#caption").css("width", w + "px");
	}



	//////////////////////////////////////////////
	//											//
	//					Return					//
	//											//
	//////////////////////////////////////////////
	//album.init();
	return album
});
