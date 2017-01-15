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
			$(".page").hide();
			$(".pageNavElem").addClass("dimmed").removeClass("current");
			$(".pageNav-" + index).addClass("current").removeClass("dimmed");
			$("#page-" + index).show();

			$(".pageNav-prev, .pageNav-next").removeClass("disabled");
			if (index === 1) { $(".pageNav-prev").addClass("disabled"); }
			if (index === maxPages) { $(".pageNav-next").addClass("disabled"); }

			updateURL(index);
			currentPage = index;
		}
	};

	var nextPage = function() { goToPage(currentPage + 1); };
	var prevPage = function() { goToPage(currentPage - 1); };

	// Updates the url to reflect the overlay we are going to
	var updateURL = function(page) {

		var base = document.URL.split("#")[0]
		var new_url = (page === 1) ? base : base + "#" + page;
		document.URL = new_url;
	};

	var getURLPage = function() { 
		return document.URL.split("#")[1]
	}


	//////////////////////////////////////////////
	//											//
	//					Return					//
	//											//
	//////////////////////////////////////////////

	return album;
});
