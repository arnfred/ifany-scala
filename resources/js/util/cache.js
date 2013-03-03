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
	
	// Given an image, the url is loaded
	cache.save = function(img) {

		// Cache image
		var url = img.urls[size.getImageSize(img).url];
		var domImg = $("<img src=\"" + url + "\"/>");
		cache.images[img.id] = domImg;

		// set callback
		$(domImg).load(function() {
			cache.nbLoaded += 1;
			radio("overlay:loaded").broadcast(cache.nbLoaded);
		});
	}

	// Given an id, the domImg is returned
	cache.load = function(img) {
		var img = cache.images[img.id]
		if (img == undefined) throw Error("Image with id " + img.id + " hasn't been cached");
		return img
	}


	return cache;

});
