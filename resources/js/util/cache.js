define(["util/size", "jquery", "radio"], function(size, $, radio) {

	//////////////////////////////////////////////
	//											//
	//				  Interface					//
	//											//
	//////////////////////////////////////////////
	var cache = {};

	//////////////////////////////////////////////
	//											//
	//				 Properties					//
	//											//
	//////////////////////////////////////////////

	cache.nbLoaded = 0;
	cache.images = {};

	//////////////////////////////////////////////
	//											//
	//			  Public Functions				//
	//											//
	//////////////////////////////////////////////
	
	// Given some media, the url is loaded
	cache.save = function(img, url) {

		// Cache media
        var id = "#id-" + img.file.replaceAll("/", "--").replaceAll(".", "--");
        var dom_media = $(id).clone().attr("sizes", "(min-width: 800) 83.34vw, 100vw");
		cache.images[img.file] = dom_media;

		// set callback
		$(dom_media).load(function() {
			cache.nbLoaded += 1;
			radio("overlay:loaded").broadcast(cache.nbLoaded);
		});
	}

	// Given an id, the domImg is returned
	cache.load = function(img) {
		if (img == undefined) throw Error("Image with name " + img.file + " hasn't been cached");
		var dom_img = cache.images[img.file]
		return dom_img
	}

	return cache;

});
