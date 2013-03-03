define(["jquery", "radio", "util/size", "lib/history", "util/cache"],
	function($, radio, size, history, cache) {

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

		// Album ready event
		radio("album:ready").subscribe(albumReady);

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
		//historyUpdate(img);
		$("#overlay").fadeIn();
		$("body").css("overflow-y", "hidden");
		resizeOverlay();
	}


	var overlayClose = function() {
		//historyClose();
		$("body").css("overflow-y", "auto");
		$("#overlay").fadeOut();
	}


	var navArrowUpdate = function(hasPrev, hasNext) {
		// Make sure we hide or show previous arrow
		if (hasPrev) $("#overlay-prev span").fadeIn("fast")
		else $("#overlay-prev span").hide();

		// Make sure we hide or show next arrow
		if (hasNext) $("#overlay-next span").fadeIn("fast")
		else $("#overlay-next span").hide();
	}


	var overlayUpdate = function(img) {
		var domImg = cache.load(img);
		$("#overlay-img img").remove();
		$("#overlay-img div").prepend(domImg);
		$("#caption").html(img.caption);
		$("#overlay-img").attr("alt",img.caption);
	}


	var albumReady = function() {
		$("div.album").fadeTo(400,1);
	}


	var resizeOverlay = function() {
		var captionHeight = $("#caption").height()
		$("div#overlay-img div").css("height", size.getHeight() - captionHeight - 4 + "px");
		var w = $("#overlay-img img").width();
		var p = ($("#overlay-img div").width() - w) / 2.0;
		if (w > 250) {
			$("#caption").css("padding", "0 " + Math.floor(p) + "px");
			$("#caption").css("width", w + "px");
		}
	}



	//////////////////////////////////////////////
	//											//
	//					Return					//
	//											//
	//////////////////////////////////////////////
	album.init();
	return album
});
