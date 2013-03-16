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
		"views/frontpage",
		"lib/scrollTo",
		"domReady!"
	],
	// The call back function
	function(frontpage, scrollTo) {

		// Initialize frontpage
		frontpage.init();
	}
)
.next(["googleAnalytics"])
