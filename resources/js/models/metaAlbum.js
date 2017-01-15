define(["radio",
		"views/metaAlbum",
		"util/cache",
		"lib/history",
		"lib/underscore",
	],
	function(radio,
		albumView,
		cache,
		history,
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



	//////////////////////////////////////////////
	//											//
	//				   Events					//
	//											//
	//////////////////////////////////////////////

	// Subscribe to the event that we click a thumbnail
	album.events = function() {

		$("span.img-container").each(function (index, im) {
			$(im).click(function () { console.debug("click: ", index, im); });
		});

		$(".pageNavElem").each(function(index, elem) { 
			$(elem).click(function() {
				var page = $(elem).data("page");
				console.debug(page);
				radio("page:change").broadcast(page);
			});
		});

		$(".pageNav-next").click(function() { radio("page:next").broadcast(); });
		$(".pageNav-prev").click(function() { radio("page:prev").broadcast(); });

	};

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
		album.url = data.url;
		album.title = document.title;
		album.events();
	};


	//////////////////////////////////////////////
	//											//
	//			  Private Functions				//
	//											//
	//////////////////////////////////////////////

	var getURLParts = function() {
		var reversed = document.URL.split("/").reverse().join("/");
		var parts = _.map(reversed.split("/" + album.url + "/"), function(p) { 
				return p.split("/").reverse().join("/"); 
		});
		return [parts[1] + "/" + album.url + "/", parts[0]];
	};

	// Updates the url to reflect the page we are going to
	var updateHistory = function(page) {

		var pageName = "page" + page;

		// Generate new url string
		var parts = getURLParts();
		var new_url = (page === null) ? parts[0] : parts[0] + pageName;

		// Check if we are already at expected state
		if (parts[1] == pageName) return;

		// Find image caption and create new title
		var title = (name === null) ? album.title : album.title + " : Page " + 1;

		// Change state to new url string
		history.pushState({ 'state_index' : history.getCurrentIndex(), 'name' : name }, title, new_url);
	};


	// Update page when we change browser history
	var respondHistory = function() {

		var state_data = history.getState().data;
		// If the state index has 1 added to it, then it's an internal state change
		if (state_data.state_index != (History.getCurrentIndex() - 1)) {
			if (state_data.name !== null) {
				var page = state_data.name.substring(4);
				radio.broadcast("page:change", page);
			}
			else {
				goToPage(1);
			}
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
