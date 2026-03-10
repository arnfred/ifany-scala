package ifany

import scalatags.Text.all.*
import HtmxAttrs.*

object GalleryTemplate {

  def apply(view: GalleryView, session: Option[Session]): String = {
    val headerExtra = nextPrevLinks(view.getNav)
    Base.page(view, session, headerExtra = Seq(headerExtra), body = frag(
      navigation(view.getNav),
      headerSection(view),
      albumList(view),
      navigation(view.getNav)
    ))
  }

  private def headerSection(view: GalleryView): Frag = {
    val albumNum = view.gallery.albums.size
    val imagesNum = view.getSize
    val coverSrcset = (for (label <- view.cover.image.versions)
      yield s"${view.cover.image.imageURL(view.cover.album.url, label)} ${view.cover.image.width(label)}w"
    ).mkString(", ")

    div(cls := s"$twoColLayout pt-8",
      div(cls := "sm:col-start-2",
        h1(cls := "text-3xl leading-tight", view.getTitle),
        p(cls := "text-sm mt-1", view.getDateString, "."),
        p(cls := "italic text-sm",
          s"This gallery contains ${albumNum.toString}",
          if (albumNum == 1) " album" else " albums",
          s" with ${imagesNum.toString} images.",
        ),
        p(cls := "mt-2 text-sm", raw(view.getDescription))
      ),
      div(cls := "sm:col-start-3",
        img(
          src := view.cover.image.imageURL(view.cover.album.url, "l"),
          srcset := coverSrcset,
          sizes := "(min-width: 1000px) 60vw, (min-width: 800px) 80vw, 100vw",
          alt := view.cover.image.description,
          cls := "w-full"
        ),
        p(cls := Seq(
          "text-right",
          "italic",
          "text-site-muted",
          "text-sm",
          "mt-1"
        ).mkString(" "),
          raw(s"""From the album "<a href="${view.cover.album.url}/" class="text-site-link hover:text-site-link-hover">${view.cover.album.title}</a>"""")
        )
      )
    )
  }

  private def albumList(view: GalleryView): Frag =
    div(cls := "mt-8",
      for (album <- view.gallery.albums) yield albumRow(view, view.gallery.url, album)
    )
}
