define(["jquery"],
	function() {

	//////////////////////////////////////////////
	//											//
	//				  Interface					//
	//											//
	//////////////////////////////////////////////
	var frontpage = {}



	//////////////////////////////////////////////
	//											//
	//				   Events					//
	//											//
	//////////////////////////////////////////////

	frontpage.events = function() {

		// Window resize
		window.onresize = resizeHeader;

		// define click event for each category
		$("div.category").each(setCategoryClick);
	}



	//////////////////////////////////////////////
	//											//
	//					Init					//
	//											//
	//////////////////////////////////////////////

	frontpage.init = function() {

		// Resize Headerr
		resizeHeader();

		// Toggle events
		frontpage.events();
	}


	//////////////////////////////////////////////
	//											//
	//			  Private Functions				//
	//											//
	//////////////////////////////////////////////

	// Resize header so it fills the space
	var resizeHeader = function() {
		var b = $("#header");
		var h = $("#header span");
		var s = $("#subheader span");
		var ratio = h.parent().width() / h.width();
		var fontsize = ratio * parseInt(h.css("font-size")) * 0.95;
		var lineheight = Math.pow(fontsize, 0.9) * 1.2;
		var subfontsize = fontsize * 0.20;

		h.css("font-size", fontsize + "px")
		s.css("font-size", subfontsize + "px")
	}


	var setCategoryClick = function(index, cat) {
		// Unfold categories
		var toggleAlbums = function(albums) { 
			albums.find("img").each(function (index, im) {
				$(im).attr("src", $(im).attr("href"));
			})
			albums.fadeToggle("fast");
		}

		var c = $(cat);
		c.click(function() { 
			$.scrollTo(c,400); 
			toggleAlbums(c.nextUntil(".category, .credits")) 
		});
	}
	


	//////////////////////////////////////////////
	//											//
	//					Return					//
	//											//
	//////////////////////////////////////////////
	return frontpage
});
