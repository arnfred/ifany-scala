define(["radio", "views/album", "util/cache", "lib/history", "lib/hammer.min", "util/foreach"], 
	function(radio, albumView, cache, history, Hammer) {

	//////////////////////////////////////////////
	//											//
	//				  Interface					//
	//											//
	//////////////////////////////////////////////
	var album = {};


	//////////////////////////////////////////////
	//											//
	//				 Properties					//
	//											//
	//////////////////////////////////////////////

	album.images = null;
	album.currentId = null;
	album.overlayActive = false;
	album.ids = [];



	//////////////////////////////////////////////
	//											//
	//				   Events					//
	//											//
	//////////////////////////////////////////////

	// Subscribe to the event that we click a thumbnail
	album.events = function() {

		$("img.frame").each(function (index, im) { 
			$(im).click(function () { setOverlayId($(im).attr("id")) });
		})

		// Broadcast resize event
		$(window).resize(function() { radio("window:resize").broadcast(); })

		// Broadcast that album is ready
		radio("album:ready").broadcast();

		// Broadcast overlay key events
		$(window).keydown(function(e){ 
			if (album.overlayActive) { overlayKeypress(e.keyCode); }
		});

		// Broadcast arrow click event
		$("#overlay-prev").click(function() { goPrev(); });
		$("#overlay-next").click(function() { goNext(); });
		$("#overlay-img").click(function() { closeOverlay() });

		// On resize, recache
		radio("window:resize").subscribe(cacheImages);

		// On swipe
		var overlay_img = document.getElementById('overlay-img');
		var credits = document.getElementById('credits');
		var hammer_options = { transform: true };
		Hammer(overlay_img, hammer_options).on("dragleft", function() { goNext(); });
		Hammer(overlay_img, hammer_options).on("dragright", function() { goPrev(); });
	}

	//////////////////////////////////////////////
	//											//
	//					Init					//
	//											//
	//////////////////////////////////////////////

	album.init = function() {

		// Init albumView
		albumView.init();

		// Get images
		images.then(function(im) { 
			album.images = im; 
			album.events();
			openOverlayIfNecessary();
		}, function() { 
			throw Error("error while fetching image data");
		})

		// Get id's
		album.ids = $("img.frame").map(function(index,im) { return $(im).attr("id") });


	}


	//////////////////////////////////////////////
	//											//
	//			  Private Functions				//
	//											//
	//////////////////////////////////////////////

	var openOverlayIfNecessary = function() {
		// Check if we have to open up an overlay directly
		var url = document.URL.split("/");
		var id = url[4];
		if (url.length > 5) createOverlay(id);
	}

	var createOverlay = function(id) {

		// Update id and state
		album.currentId = id;
		album.overlayActive = true;

		// Exit if array doesn't contain an object
		if (album.images[id] == undefined) { closeOverlay(); return }

		// Get image and prev and next ids
		var img = album.images[id];
		var prev = getPrevId(id);
		var next = getNextId(id);

		// cache images used but make sure to cache the current image first
		cache.save(img)
		cacheImages();


		// Broadcast
		radio("overlay:set").broadcast(img, (prev != -1), (next != -1));
	}


	var overlayInit = function(id) {
		album.overlayActive = true;
		cacheImages();
		var img = album.images[album.currentId];
		overlayUpdate(img);
	}

	var overlayUpdate = function(img) {
		album.currentId = img.id;
		var prevId = getPrevId(album.currentId);
		var nextId = getNextId(album.currentId);

		radio("overlay:set").broadcast(img, (prevId != -1), (nextId != -1));
	}

	var overlayKeypress = function(keyCode) {

		// Previous image
		if (keyCode == 37 || keyCode == 8) goPrev();

		// Next image
		else if ((keyCode == 39) || (keyCode == 32) || (keyCode == 13)) goNext();

		// Close overlay
		else if (keyCode == 27) closeOverlay();
	}


	var goNext = function() {
		if (album.overlayActive == false) return
		var nextId = getNextId(album.currentId);
		if (nextId != -1) {
			setOverlayId(nextId);
			//var img = album.images[nextId];
			//overlayUpdate(img);
		}
	}


	var goPrev = function() {
		if (album.overlayActive == false) return
		var prevId = getPrevId(album.currentId);
		if (prevId != -1) {
			setOverlayId(prevId);
			// var img = album.images[prevId];
			// overlayUpdate(img);
		}
	}


	// Updates the url to reflect the overlay we are going to
	var setOverlayId = function(id) {

		// Update the current overlay id
		album.currentId = id;

		// Generate new url string
		var oldUrl = document.URL.split("/");
		var newUrl = oldUrl.join("/");
		if (!album.overlayActive) newUrl = oldUrl.slice(0,-1).join("/");
		else newUrl = oldUrl.slice(0,-2).join("/");

		// Change state to new url string
		history.pushState(null, null, newUrl + "/" + id + "/");
		createOverlay(id)
	}


	// Updates the url and sets overlayActive to false
	var closeOverlay = function() {

		// Update state
		album.currentId = null;
		album.overlayActive = false;

		// Update url
		var newUrl = document.URL.split("/").slice(0,-2).join("/") + "/";
		history.pushState(null, null, newUrl);

		// Broadcast close event
		radio("overlay:close").broadcast();
	}




	var getNextId = function(id) {
		var next = album.ids.filter(function(index) { 
			return album.ids[index-1] == id;
		})
		return (next.length == 0) ? -1 : next[0]
	}

	var getPrevId = function(id) {
		var next = album.ids.filter(function(index) { 
			return album.ids[index+1] == id;
		})
		return (next.length == 0) ? -1 : next[0]
	}

	var cacheImages = function() {
		for (var id in album.images) {
			cache.save(album.images[id]);
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
