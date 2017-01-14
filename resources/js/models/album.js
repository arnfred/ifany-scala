define(["radio",
		"views/album",
		"util/cache",
		"lib/history",
		"lib/underscore",
		//"lib/hammer.min",
	],
	function(radio,
		albumView,
		cache,
		history,
		_
		//Hammer
	) {

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
	album.names = null;
	album.url = null;
	album.current_image = null;
	album.title = null;
	album.overlayActive = false;



	//////////////////////////////////////////////
	//											//
	//				   Events					//
	//											//
	//////////////////////////////////////////////

	// Subscribe to the event that we click a thumbnail
	album.events = function() {

		$("span.img-container").each(function (index, im) {
			$(im).click(function () { createOverlay($(im).attr("id").substring(2)) });
		})

		// Broadcast resize event
		lazy_resize = _.debounce(function() { radio("window:resize").broadcast(); }, 300);
		$(window).resize(lazy_resize)

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
		//Hammer(overlay_img, hammer_options).on("dragleft", function() { goNext(); });
		//Hammer(overlay_img, hammer_options).on("dragright", function() { goPrev(); });

		// Change in browser history
		//history.Adapter.bind(window, 'statechange', respondHistory)
	}

	//////////////////////////////////////////////
	//											//
	//					Init					//
	//											//
	//////////////////////////////////////////////

	album.init = function() {

		// Init albumView
		albumView.init();

		// Get data
		album.images = _.indexBy(data.images, "file");
		album.names = _.pluck(data.images, "file");
		album.url = data.url
		album.title = document.title;
		album.events();
		openOverlayIfNecessary();
	}


	//////////////////////////////////////////////
	//											//
	//			  Private Functions				//
	//											//
	//////////////////////////////////////////////

	var getURLParts = function() {
        var reversed = document.URL.split("/").reverse().join("/");
        var parts = _.map(reversed.split("/" + album.url + "/"),
                          function(p) { return p.split("/").reverse().join("/"); })
		return [parts[1] + "/" + album.url + "/", parts[0]]
	}

	var openOverlayIfNecessary = function() {
		// Check if we have to open up an overlay directly
		name = getURLParts()[1];
		if (name != "" && album.images[name] != undefined) {
			createOverlay(name)
		}
	}

	var createOverlay = function(name) {

		// Update browser location
		updateHistory(name);

		// Update id and state
		album.current_name = name;
		album.overlayActive = true;

		// Exit if array doesn't contain an object
		if (album.images[name] == undefined) { closeOverlay(); return }

		// Get image and prev and next names
		var img = album.images[name];
		var prev = getPrevImg(name);
		var next = getNextImg(name);

		// cache surrounding images
		cacheImages();

		// Broadcast
		radio("overlay:set").broadcast(img, (prev != undefined), (next != undefined));
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
		var next_name = getNextImg(album.current_name);
		if (next_name != undefined) {
			createOverlay(next_name);
		}
	}


	var goPrev = function() {
		if (album.overlayActive == false) return
		var prev_name = getPrevImg(album.current_name);
		if (prev_name != undefined) {
			createOverlay(prev_name);
		}
	}


	// Updates the url to reflect the overlay we are going to
	var updateHistory = function(name) {

		// Generate new url string
		var parts = getURLParts();
		var new_url = (name == null) ? parts[0] : parts[0] + name;

		// Check if we are already at expected state
		if (parts[1] == name) return;

		// Find image caption and create new title
		var title = (name == null) ? album.title : album.title + " : " + album.images[name].description;

		// Change state to new url string
		history.pushState({ 'state_index' : history.getCurrentIndex(), 'name' : name }, title, new_url);
	}


	// Update page when we change browser history
	var respondHistory = function() {

		var state_data = history.getState().data;
		// If the state index has 1 added to it, then it's an internal state change
		if (state_data.state_index != (History.getCurrentIndex() - 1)) {
			if (state_data.name != null) {
				createOverlay(state_data.name);
			}
			else {
				closeOverlay();
			}
		}
	}


	// Updates the url and sets overlayActive to false
	var closeOverlay = function() {

		// Update state
		album.current_name = null;
		album.overlayActive = false;

		// Update url
		updateHistory(null);

		// Broadcast close event
		radio("overlay:close").broadcast();
	};




	var getNextImg = function(name) {
		var index = album.names.indexOf(name);
		var last_index = album.names.length - 1;
		return (index === -1 || index === last_index) ? undefined : album.names[index+1];
	};

	var getPrevImg = function(name) {
		var index = album.names.indexOf(name);
		return (index === -1 || index === 0) ? undefined : album.names[index-1];
	};

	var cacheImages = function() {
		if (album.overlayActive) {
			var next = getNextImg(album.current_name);
			var nextnext = getNextImg(next);
			var prev = getPrevImg(album.current_name);
			cache.save(album.images[album.current_name], album.url);
			if (next !== undefined) cache.save(album.images[next], album.url);
			if (nextnext !== undefined) cache.save(album.images[nextnext], album.url);
			if (prev !== undefined) cache.save(album.images[prev], album.url);
		}
	};


	//////////////////////////////////////////////
	//											//
	//					Return					//
	//											//
	//////////////////////////////////////////////
	album.init();
	return album;
});
