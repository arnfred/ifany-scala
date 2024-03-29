define(["jquery",
        "radio",
		"lib/underscore"],
	function($, radio, _) {

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

    album.type = null;



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

	album.init = function(albumType) {

        // Toggle state
        album.type = albumType;

		// Toggle events
		album.events();

        // Ensure that videos play when the mouse hovers over them
        playVideosOnHover();

        // Only start preloading the videos when they enter the viewport
        var lazy_load_videos = _.debounce(function() { loadVideosIfVisible(); }, 300)
        $(window).on('scroll', lazy_load_videos);
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
	};


	var overlayClose = function() {
		$("body").css("overflow-y", "auto");
		$("#overlay").fadeOut();
	};


	var navArrowUpdate = function(hasPrev, hasNext) {
		// Make sure we hide or show previous arrow
		if (hasPrev) $("#overlay-prev").fadeIn(300);
		else $("#overlay-prev").hide();

		// Make sure we hide or show next arrow
		if (hasNext) $("#overlay-next").fadeIn(300);
		else $("#overlay-next").hide();
	};


	var overlayUpdate = function(img) {
        var id = "#id-" + img.file.replaceAll("/", "--").replaceAll(".", "--");
        var dom_img = $(id).clone().attr("sizes", "(min-width: 800) 83.34vw, 100vw");
		var album_name = img.file.split("/")[0];
        if (album.type === "meta") {
            var description = img.description + " [<a target=\"_blank\" href=\"/photos/" + album_name + "\" class=\"image-link\" onClick=\"arguments[0].stopPropagation()\">Album</a>]"
        } else {
            var description = img.description
        }

		$("#overlay-img .media").remove();
		$("#overlay-img").children("div").prepend(dom_img);
		$("#caption").html(description);
	};


    var resizeOverlay = function() {
        var captionHeight = $("#caption").height();
        $("span#caption").css("top", "-" + (captionHeight + 11) + "px");
        var img = $("#overlay-img .media");
        var vpRatio = (window.innerWidth*(10.0/12.0)) / window.innerHeight;
        img.load(function() {
            var imgRatio = this.width / this.height;
            if (imgRatio >= vpRatio) {
                $("#overlay-img .media").css("width", "100%");
                $("span#caption").css("width", "100%");
            }
            else {
                var width = (imgRatio/vpRatio)*100;
                $("#overlay-img .media").css("width", width + "%");
                $("span#caption").css("width", width + "%");
            }
        });
        $(img).on('loadedmetadata', function() {
            var imgRatio = this.videoWidth / this.videoHeight;
            if (imgRatio >= vpRatio) {
                $("#overlay-img .media").css("width", "100%");
                $("span#caption").css("width", "100%");
            }
            else {
                var width = (imgRatio/vpRatio)*100;
                $("#overlay-img .media").css("width", width + "%");
                $("span#caption").css("width", width + "%");
            }
        });
    };

    var playVideosOnHover = function() {
        $('video').each(function() {
            // Play video on mouseover
            $(this).on('mouseover', function() {
                $(this).get(0).play();
            });

            // Pause video on mouseleave
            $(this).on('mouseleave', function() {
                $(this).get(0).pause();
            });
        });
    }

    var loadVideosIfVisible = function() {
        $('video').each(function() {
            var video = $(this); // Current video in the loop
            if (isElementInViewport(video[0])) { // Convert jQuery object to DOM element
                console.log("Video is in viewport", video);
                video.attr('preload', 'auto');
            }
        });

    };

    var isElementInViewport = function(el) {
        var rect = el.getBoundingClientRect();
        return (
            rect.top >= 0 &&
            rect.left >= 0 &&
            rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
            rect.right <= (window.innerWidth || document.documentElement.clientWidth)
        );
    };

	//////////////////////////////////////////////
	//											//
	//					Return					//
	//											//
	//////////////////////////////////////////////

	return album;
});
