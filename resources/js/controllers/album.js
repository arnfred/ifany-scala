// Curl
curl(

	// Set up the configuration
	{
		baseUrl: '/js',
		paths: {
			"css": "./../css/",
			"jquery": "lib/jquery/jquery",
			"radio": "lib/radio/radio.min"
		},
	}, 

	// Set up the required modules
	[
		"jquery"
	],

	function($) {
		// Polluting the global namespace, I know
		images = $.getJSON("/album/" + albumData.id + "/")
	}
)
.next(
	[
		"models/album",
		"views/album",
		"domReady!"
	],

	// The call back function
	function(albumModel, albumView) {

		// Initialize album
		//albumView.init();

	}
)
.next(["googleAnalytics"])
