package ifany

case class FrontpageTemplate(view : FrontpageView) extends Template {

  // Implicit conversion 
  import com.dongxiguo.fastring.Fastring.Implicits._
  implicit val v = view

  override def toString : String = Base(Template(header + categories))

  def header : Template = Template(fast"""
    <div class="row-fluid top topmost">
        <div class="span3 offset1" id="about">
            <h1 id="header"><span><span id="if">if</span><span id="any">any</span></span></h1>
            <h4 id="subheader"><span>photography</span></h4>

            <p id="about-text">The photos on this site are an ongoing collection of things, people
            and places that happen to stand in my way the moment I press the
            shutter. They are licensed under a copyleft license (<a
                href="http://creativecommons.org/licenses/by-sa/3.0/" alt="Creative
                Commons CC-BY-SA">CC-BY-SA</a>) which means that as long as you
            credit me and keep any deriviative work under the same license, you can
            use the photos for whatever you want. Inquiries are welcome at <a
                href="mailto:jonas@ifany.org"
                alt="jonas@ifany.org">jonas@ifany.org</a>.</p>

        </div>

        <div class="span7" id="image">
            <img src="${ view.banner.urls("large") }" class="frame"/>
        </div>
    </div>
  """)


  def categories : Template = Template({
    for ((cat, albums) <- view.getCategories) yield {
       val cover = view.getRandCover(albums)
       category(cat, cover, albums) + categoryAlbums(albums)
    }
  }.mkString)

  def category(cat : Category, cover : Image, albums : List[Album]) : Template = Template { 
    fast"""
      <div class="row-fluid category">
          <div class="span3 offset1 cat-image">
              <img src="${ cover.urls("small") }" class="frame"/>
          </div>

          <div class="span7" class="cat-info">
              <h2 class="cat-title">${ cat.name }</h2>
              <p class="cat-date">${ view.getCatDateString(albums) }.</p>
              <p class="cat-meta">This gallery contains 
                <span class="num">${ albums.size }</span> 
                ${ if (albums.size == 1) "album" else "albums" } with 
                <span class="num">${ view.getCatNumImages(albums) }</span> images.
              </p>
              <p class="cat-desc">${ cat.description }</p>
          </div>
      </div>
    """
  }

  def categoryAlbums(albums : List[Album]) : Template = Template {
    (for (album <- albums) yield fast"""
      <div class="row-fluid album">
        <a href="/${ album.url }/">
          <div class="span3 offset1 album-info">
            <h3 class="album-title">${ album.title }</h3>
            <p class="album-date">${ view.getAlbumDateString(album) }</p>
            <p class="album-meta">
              <span class="num">${ view.getAlbumSize(album) }</span> 
              ${ if (view.getAlbumSize(album) == 1) "Image" else "Images" } 
            </p>
          </div>

          <div class="span7 album-images">
            <div class="row-fluid">
              ${ albumThumbnails(album) }
            </div>
          </div>
          <div class="span1 album-arrow hidden-phone">
            <p>&raquo;</p>
          </div>
        </a>
      </div>
    """).mkString
  }

  def albumThumbnails(album : Album) : Template = Template {
    (for (image <- view.getAlbumImages(album, 4)) yield fast"""
      <div class="span3 img">
        <img href="${ image.urls("thumb") }" class="frame" src="/img/loader.gif"/>
      </div>
    """).mkString
  }
}
