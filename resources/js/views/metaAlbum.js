define(["jquery", "radio", "util/size", "util/cache", "lib/history"],
	function($, radio, size, cache, history) {

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
		radio("page:change").subscribe(goToPage);
		radio("page:next").subscribe(nextPage);
		radio("page:prev").subscribe(prevPage);
		radio("overlay:set").subscribe(overlayChange);
		radio("overlay:close").subscribe(overlayClose);
		radio("window:resize").subscribe(resizeOverlay);


	};



	//////////////////////////////////////////////
	//											//
	//					Init					//
	//											//
	//////////////////////////////////////////////

	album.init = function() {

		// Toggle events
		album.events();

		// Initialise page navigation
		var page = isNaN(parseInt(getURLPage())) ? 1 : parseInt(getURLPage());
		goToPage(page);
	};


	//////////////////////////////////////////////
	//											//
	//			  Private Functions				//
	//											//
	//////////////////////////////////////////////

	var goToPage = function(index) {
		var maxPages = $(".page").length;
		if (index === currentPage || index <= 0 || index > maxPages) { return; }
		else {

			if ($(".page").is(":visible")) {
				$(".page").fadeOut(400, function() {
					$("#page-" + index).fadeIn(600);
				});
			} else {
				$("#page-" + index).fadeIn(600);
			}


			$(".pageNavElem").addClass("dimmed").removeClass("current");
			$(".pageNav-" + index).addClass("current").removeClass("dimmed");

			$(".pageNav-prev, .pageNav-next").removeClass("disabled");
			if (index === 1) { $(".pageNav-prev").addClass("disabled"); }
			if (index === maxPages) { $(".pageNav-next").addClass("disabled"); }

			updateURL(index);
			currentPage = index;
			$("body").scrollTop(0);
		}
	};

	var nextPage = function() { goToPage(currentPage + 1); };
	var prevPage = function() { goToPage(currentPage - 1); };

	// Updates the url to reflect the overlay we are going to
	var updateURL = function(page) {

		var base = document.URL.split("#")[0];
		var new_url = (page === 1) ? base : base + "#" + page;
		history.replaceState({}, document.title, new_url);
	};

	var getURLPage = function() { 
		return document.URL.split("#")[1];
	};

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
		var dom_img = cache.load(img);
		var album = img.file.split("/")[0];
		$("#overlay-img img").remove();
		$("#overlay-img div").prepend(dom_img);
		$("#caption").html(img.description + " [<a target=\"_blank\" href=\"/photos/" + album + "\" class=\"image-link\" onClick=\"arguments[0].stopPropagation()\">Album</a>]");
		$("#overlay-img img").attr("alt",img.description);
	};


	var resizeOverlay = function() {
        var captionHeight = $("#caption").height();
        $("span#caption").css("top", "-" + (captionHeight + 11) + "px");
        var img = $("#overlay-img img");
        img.load(function() {
            var imgRatio = this.width / this.height;
            var vpRatio = (size.getWidth()*(10.0/12.0)) / size.getHeight();
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
            var vpRatio = (size.getWidth()*(10.0/12.0)) / size.getHeight();
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


	//////////////////////////////////////////////
	//											//
	//					Return					//
	//											//
	//////////////////////////////////////////////

	return album;
});
