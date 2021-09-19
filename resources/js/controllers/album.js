// Curl
curl(

	// Set up the configuration
	{
		baseUrl: '/js',
		paths: {
			"css": "./../css/",
			"jquery": "lib/jquery/jquery",
			"radio": "lib/radio/radio.min",
		},
	}, 

	// Set up the required modules
	[
		"jquery",
		"models/album",
		"views/album",
		"domReady!"
	],

	// The call back function
	function($, albumModel, albumView) {
		// Nothing to prepare here
	}
)
