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
	cache.urlSize = {
		tiny	: "Ti",
		thumb	: "Th",
		small	: "S",
		medium	: "M",
		large	: "L",
		xlarge	: "XL",
		x2large	: "X2",
		x3large	: "X3"
	}

	//////////////////////////////////////////////
	//											//
	//			  Public Functions				//
	//											//
	//////////////////////////////////////////////
	
	// Given an image, the url is loaded
	cache.save = function(img, url) {

		// Cache image
        if (img.is_video) {
            var img_url = getVideoURL(img, url);
            var dom_img = $("<video control autoplay loop><source type=\"video/mp4\" src=\"" + img_url + "\"></video>");
        } else {
            var img_url = getImgURL(img, url);
            var dom_img = $("<img src=\"" + img_url + "\"/>");
        }
		cache.images[img.file] = dom_img;

		// set callback
		$(dom_img).load(function() {
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


	//////////////////////////////////////////////
	//											//
	//			  Private Functions				//
	//											//
	//////////////////////////////////////////////
	
	var getImgURL = function(img, url) {
		return "/photos/" + url + "/" + img.file + "_" + size.getImageSize(img).url + ".jpg"
	}

    var getVideoURL = function(img, url) {
		return "/photos/" + url + "/" + img.file + ".mp4"
    }


	return cache;

});
