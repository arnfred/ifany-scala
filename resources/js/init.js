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
		"jquery",
		"css!css/bootstrap.min.css", 
		"css!css/bootstrap-responsive.min.css", 
		"css!css/global",
		"css!css/frontpage"
	]
)
.next(
	[
		"views/frontpage",
		"domReady!"
	],

	// The call back function
	function(frontpage) {

		// Initialize frontpage
		frontpage.init();

	}
)
