package ifany

import scalatags.Text.all.*
import HtmxAttrs.*

object AlbumTemplate {

  def apply(view: AlbumView, session: Option[Session]): String = {
    val headerExtra = Seq(
      script(raw(s"data = ${view.getJson}")),
      nextPrevLinks(view.getNav)
    )
    Base.page(view, session, headerExtra = headerExtra, body = frag(
      albumNavigation(view.getNav),
      overlayMarkup,
      albumBody(view),
      albumNavigation(view.getNav),
      lightboxScript
    ))
  }

  private def overlayMarkup: Frag =
    div(
      id := "overlay",
      cls := Seq(
        "hidden",
        "fixed",
        "inset-0",
        "z-50",
        "bg-site-overlay-bg"
      ).mkString(" "),
      // Nav arrows overlay on left/right edges
      div(id := "overlay-prev", cls := s"$overlayNavCls left-0",
        span(cls := "inline-block select-none", style := "border:12px solid transparent;border-width:12px 18px;border-right-color:currentColor")
      ),
      div(id := "overlay-next", cls := s"$overlayNavCls right-0",
        span(cls := "inline-block select-none", style := "border:12px solid transparent;border-width:12px 18px;border-left-color:currentColor")
      ),
      // Image area fills the entire overlay
      div(
        id := "overlay-img",
        cls := Seq(
          "absolute",
          "inset-0",
          "flex",
          "items-center",
          "justify-center"
        ).mkString(" "),
        div(
          id := "overlay-center-box",
          cls := "relative inline-block",
          img(cls := Seq(
            "media",
            "max-h-screen",
            "max-w-full"
          ).mkString(" "), crossorigin := "anonymous", alt := "Overlay image"),
          div(id := "caption-box", cls := Seq(
            "absolute",
            "bottom-0",
            "left-0",
            "right-0",
            "text-center"
          ).mkString(" "),
            span(
              id := "caption",
              cls := Seq(
                "inline-block",
                "text-white",
                "bg-gray-800/70",
                "backdrop-blur-sm",
                "px-3",
                "py-1",
                "text-sm"
              ).mkString(" ")
            )
          )
        )
      )
    )

  private def albumBody(view: AlbumView): Frag =
    div(cls := "pt-8",
      div(cls := Seq(
        "max-w-md",
        "mx-auto",
        "mb-8",
        "px-4"
      ).mkString(" "),
        h2(cls := "text-2xl leading-tight", view.getTitle),
        p(cls := Seq(
          "italic",
          "text-sm",
          "text-site-muted",
          "mt-1"
        ).mkString(" "), raw(view.getGalleries)),
        p(cls := "mt-2 text-sm hyphens-auto", raw(view.getDescription)),
        p(cls := Seq(
          "italic",
          "text-sm",
          "text-site-muted",
          "text-right",
          "mt-1"
        ).mkString(" "), view.getDateString)
      ),
      thumbnails(view)
    )

  private def thumbnails(view: AlbumView): Frag =
    frag(view.getRows(view.album.images).map {
      case CoverRow(image) => coverRow(view, image)
      case r: DualRow => twoImageRow(view, r)
    }*)

  private def coverRow(view: AlbumView, image: Image, extraCls: String = ""): Frag =
    div(cls := s"$albumRowCls $extraCls",
      imageBox(view, image, 100.0)
    )

  private def twoImageRow(view: AlbumView, row: DualRow): Frag = {
    val a1 = row.left.size(0).toDouble / row.left.size(1).toDouble
    val a2 = row.right.size(0).toDouble / row.right.size(1).toDouble

    frag(
      // Mobile: stacked (each full width)
      coverRow(view, row.left, "sm:hidden"),
      coverRow(view, row.right, "sm:hidden"),
      // Desktop: side by side with flex-grow = aspect ratio
      div(cls := Seq(
        albumRowCls,
        "hidden",
        "sm:flex",
        "gap-0.5"
      ).mkString(" "),
        div(cls := "min-w-0", style := s"flex:$a1 1 0%",
          imageBox(view, row.left, a1 / (a1 + a2) * 100)
        ),
        div(cls := "min-w-0", style := s"flex:$a2 1 0%",
          imageBox(view, row.right, a2 / (a1 + a2) * 100)
        )
      )
    )
  }

  private def imageBox(view: AlbumView, image: Image, ratio: Double): Frag = {
    if (image.is_video)
      videoBox(view, image)
    else
      imgBox(view, image, ratio)
  }

  private def videoBox(view: AlbumView, image: Image): Frag =
    video(
      cls := s"media $imgCls",
      id := image.id,
      attr("file") := image.file,
      attr("muted") := "muted",
      attr("loop") := "loop",
      attr("controls") := "controls",
      crossorigin := "anonymous",
      playsinline := "true",
      preload := "none",
      poster := image.imageURL(view.album.url, "800"),
      attr("onmouseover") := "this.play()",
      attr("onmouseleave") := "this.pause()",
      tag("source")(src := image.videoURL(view.album.url), `type` := "video/mp4")
    )

  private def imgBox(view: AlbumView, image: Image, ratio: Double): Frag = {
    val srcsetVal = (for (label <- image.versions)
      yield s"${image.imageURL(view.album.url, label)} ${image.width(label)}w"
    ).mkString(", ")

    img(
      cls := s"media $imgCls",
      id := image.id,
      attr("file") := image.file,
      src := image.imageURL(view.album.url, "800"),
      srcset := srcsetVal,
      crossorigin := "anonymous",
      loading := "lazy",
      sizes := s"(min-width: 800px) ${ratio * 0.8}vw, 100vw",
      alt := image.description
    )
  }

  private def lightboxScript: Frag =
    script(`type` := "module", raw("""
      import lightbox from '/js/lightbox.js'
      lightbox.init(data)
    """))
}
