define(["jquery", "radio"],
	function($, radio) {

	//////////////////////////////////////////////
	//											//
	//				  Interface					//
	//											//
	//////////////////////////////////////////////
	var album = {};
	var currentPage;



	//////////////////////////////////////////////
	//											//
	//				   Events					//
	//											//
	//////////////////////////////////////////////

	album.events = function() {

		// Window resize event
		radio("overlay:set").subscribe(overlayChange);
		radio("overlay:close").subscribe(overlayClose);
		radio("window:resize").subscribe(resizeOverlay);
		radio("overlay:loaded").subscribe(printLoaded);


	};



	//////////////////////////////////////////////
	//											//
	//					Init					//
	//											//
	//////////////////////////////////////////////

	album.init = function() {

		// Toggle events
		album.events();
	};


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
		var album = img.file.split("/")[0];
		$("#overlay-img .media").remove();
		$("#overlay-img").children("div").prepend(dom_img);
		$("#caption").html(img.description + " [<a target=\"_blank\" href=\"/photos/" + album + "\" class=\"image-link\" onClick=\"arguments[0].stopPropagation()\">Album</a>]");
	};


	var resizeOverlay = function() {
        var captionHeight = $("#caption").height();
        $("span#caption").css("top", "-" + (captionHeight + 11) + "px");
        var img = $("#overlay-img .media");
        var vpRatio = (window.innerWidth*(10.0/12.0)) / window.innerHeight;
        img.load(function() {
            var imgRatio = this.width / this.height;
            if (imgRatio >= vpRatio) {
                $("#overlay-img img").css("width", "100%");
                $("span#caption").css("width", "100%");
            }
            else {
                var width = (imgRatio/vpRatio)*100;
                $("#overlay-img img").css("width", width + "%");
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

    var printLoaded = function(img) {
        console.log("Loaded: ", img);
    };


	//////////////////////////////////////////////
	//											//
	//					Return					//
	//											//
	//////////////////////////////////////////////

	return album;
});
