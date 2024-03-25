define(["radio",
		"views/album",
		"lib/underscore",
	],
	function(radio,
		albumView,
		_
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
	album.title = null;
	album.current_image = null;
	album.overlayActive = false;


	//////////////////////////////////////////////
	//											//
	//				   Events					//
	//											//
	//////////////////////////////////////////////

	// Subscribe to the event that we click a thumbnail
	album.events = function() {

		$("img.media").each(function (index, im) {
			$(im).click(function () { 
				createOverlay($(im).attr("file"));
			});
		});

		// Broadcast resize event
		lazy_resize = _.debounce(function() { radio("window:resize").broadcast(); }, 300);
		$(window).resize(lazy_resize);

		// Broadcast overlay key events
		$(window).keydown(function(e){
			if (album.overlayActive) { overlayKeypress(e.keyCode); }
		});

		// Broadcast arrow click event
		$("#overlay-prev").click(function() { goPrev(); });
		$("#overlay-next").click(function() { goNext(); });
		$("#overlay-img").click(function() { closeOverlay(); });
	};

	//////////////////////////////////////////////
	//											//
	//					Init					//
	//											//
	//////////////////////////////////////////////

	album.init = function() {

		// Init albumView
		albumView.init("meta");

		// Get data
		album.images = _.indexBy(data.images, "file");
		album.names = _.pluck(data.images, "file");
		album.url = data.url;
		album.title = document.title;
		album.events();
		openOverlayIfNecessary();
	};


	//////////////////////////////////////////////
	//											//
	//			  Private Functions				//
	//											//
	//////////////////////////////////////////////

	var getURLParts = function() {
		var splitHash = document.URL.split("#");
		var parts = splitHash[0].split("/");
		var base = "http://" + parts[2] + "/" + parts[3];
		var hash = splitHash[1] === undefined ? "" : "#" + splitHash[1];
		var image = parts[4] === undefined ? "" : parts[4];
		return [base, image, hash];
	};

	var openOverlayIfNecessary = function() {
		// Check if we have to open up an overlay directly
		name = getURLParts()[1];
		if (name !== "" && album.images[name] !== undefined) {
			createOverlay(name);
		}
	};

	var createOverlay = function(name) {

		// Update id and state
		album.current_name = name;
		album.overlayActive = true;

		// Exit if array doesn't contain an object
		if (album.images[name] === undefined) { closeOverlay(); return; }

		// Get image and prev and next names
		var img = album.images[name];
		var prev = getPrevImg(name);
		var next = getNextImg(name);

		// Broadcast
		radio("overlay:set").broadcast(img, (prev !== undefined), (next !== undefined));
	};


	var overlayKeypress = function(keyCode) {

		// Previous image
		if (keyCode == 37 || keyCode == 8) goPrev();

		// Next image
		else if ((keyCode == 39) || (keyCode == 32) || (keyCode == 13)) goNext();

		// Close overlay
		else if (keyCode == 27) closeOverlay();
	};


	var goNext = function() {
		if (album.overlayActive === false) return;
		var next_name = getNextImg(album.current_name);
		if (next_name !== undefined) {
			createOverlay(next_name);
		}
	};


	var goPrev = function() {
		if (album.overlayActive === false) return;
		var prev_name = getPrevImg(album.current_name);
		if (prev_name !== undefined) {
			createOverlay(prev_name);
		}
	};

	// Updates the url and sets overlayActive to false
	var closeOverlay = function() {

		// Update state
		album.current_name = null;
		album.overlayActive = false;

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

	//////////////////////////////////////////////
	//											//
	//					Return					//
	//											//
	//////////////////////////////////////////////
	album.init();
	return album;
});
