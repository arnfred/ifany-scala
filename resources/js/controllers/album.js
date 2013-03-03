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
		//"googleAnalytics", 
		"jquery",
		"css!css/bootstrap.min.css", 
		"css!css/bootstrap-responsive.min.css", 
		"css!css/global",
		"css!css/album",
		"css!css/overlay"
	]
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
