package ifany

import scalatags.Text.all.*
import scalatags.Text.tags2

object HtmxAttrs {

  // Shared template fragments

  private def prevLink(nav: Navigation): Frag =
    nav.prev.map(p => a(
      href := s"/${p.url}/",
      cls := navLinkCls,
      span(cls := "inline-block mr-1", style := "border:5px solid transparent;border-width:5px 8px;border-right-color:currentColor"),
      span(cls := "hidden sm:inline", p.title)
    )).getOrElse(frag())

  private def nextLink(nav: Navigation): Frag =
    nav.next.map(n => a(
      href := s"/${n.url}/",
      cls := navLinkCls,
      span(cls := "hidden sm:inline", n.title),
      span(cls := "inline-block ml-1", style := "border:5px solid transparent;border-width:5px 8px;border-left-color:currentColor")
    )).getOrElse(frag())

  private val homeLinkCls = Seq(
    "text-site-muted",
    "hover:text-site-text",
    "no-underline",
    "text-lg"
  ).mkString(" ")

  def navigation(nav: Navigation): Frag = {
    if (nav.prev.isEmpty && nav.next.isEmpty) frag()
    else div(cls := Seq(
      "grid",
      "grid-cols-3",
      "sm:grid-cols-[1fr_3fr_3fr_1fr_3fr_1fr]",
      "gap-x-8",
      "px-4",
      "mt-8",
      "items-center"
    ).mkString(" "),
      div(cls := "sm:col-start-2", prevLink(nav)),
      div(cls := "sm:col-start-3 text-center",
        a(href := "/", cls := homeLinkCls, "Home")
      ),
      div(cls := "sm:col-start-5 text-right", nextLink(nav))
    )
  }

  def albumNavigation(nav: Navigation): Frag = {
    if (nav.prev.isEmpty && nav.next.isEmpty) frag()
    else div(cls := Seq(
      "max-w-[1200px]",
      "mx-auto",
      "px-2",
      "flex",
      "items-center",
      "mt-8"
    ).mkString(" "),
      div(cls := "w-1/3", prevLink(nav)),
      div(cls := "w-1/3 text-center",
        a(href := "/", cls := homeLinkCls, "Home")
      ),
      div(cls := "w-1/3 text-right", nextLink(nav))
    )
  }

  def nextPrevLinks(nav: Navigation): Frag = {
    val next = nav.next.map(_.url).getOrElse("/")
    val prev = nav.prev.map(_.url).getOrElse("/")
    frag(
      link(rel := "next", href := next),
      link(rel := "prev", href := prev)
    )
  }

  def albumRow(view: View, albumPath: String, album: Album): Frag = {
    val albumSize = view.getAlbumSize(album)
    a(href := s"/$albumPath/${album.url}/",
      cls := Seq(
        "block",
        "no-underline",
        "text-site-secondary",
        "hover:text-site-text",
        "hover:bg-site-hover-bg",
        "transition-colors"
      ).mkString(" "),
      div(cls := s"$twoColLayout py-3",
        div(cls := Seq(
          "sm:col-start-2",
          "text-right",
          "hidden",
          "sm:block"
        ).mkString(" "),
          h3(cls := "text-base font-normal leading-tight", album.title),
          p(cls := "text-sm text-site-muted", view.getAlbumDateString(album)),
          p(cls := "italic text-sm text-site-muted",
            albumSize.toString,
            if (albumSize == 1) " Image" else " Images"
          )
        ),
        div(cls := "sm:col-start-2 sm:hidden",
          h3(cls := "text-base font-normal", album.title),
          p(cls := "text-sm text-site-muted",
            view.getAlbumDateString(album), ". ",
            span(cls := "italic",
              albumSize.toString,
              if (albumSize == 1) " Image" else " Images"
            )
          )
        ),
        div(cls := "sm:col-start-3",
          div(cls := Seq(
            "grid",
            "grid-cols-3",
            "sm:grid-cols-4",
            "gap-1"
          ).mkString(" "),
            albumThumbnails(view, album)
          )
        )
      )
    )
  }

  def albumThumbnails(view: View, album: Album): Frag = {
    val images = view.getAlbumImages(album, 4).toSeq
    frag(
      for ((image, i) <- images.zipWithIndex) yield {
        val hiddenOnMobile = if (i >= 3) "hidden sm:block" else ""
        div(cls := hiddenOnMobile,
          img(
            src := image.imageURL(album.url, "s"),
            cls := Seq(
              "w-full",
              "aspect-square",
              "object-cover"
            ).mkString(" "),
            loading := "lazy"
          )
        )
      }
    )
  }
  // htmx attributes
  val hxGet = attr("hx-get")
  val hxPost = attr("hx-post")
  val hxDelete = attr("hx-delete")
  val hxPut = attr("hx-put")
  val hxTarget = attr("hx-target")
  val hxSwap = attr("hx-swap")
  val hxTrigger = attr("hx-trigger")
  val hxConfirm = attr("hx-confirm")
  val hxVals = attr("hx-vals")
  val hxIndicator = attr("hx-indicator")

  // HTML attributes missing from ScalaTags defaults
  // Note: crossorigin is already in scalatags.Text.all
  val loading = attr("loading")
  val sizes = attr("sizes")
  val srcset = attr("srcset")
  val poster = attr("poster")
  val playsinline = attr("playsinline")
  val preload = attr("preload")

  // Shared Tailwind class vals
  val twoColLayout = Seq(
    "grid",
    "grid-cols-1",
    "sm:grid-cols-[1fr_3fr_7fr_1fr]",
    "gap-x-8",
    "px-4"
  ).mkString(" ")

  val centeredCol = Seq(
    "max-w-5xl",
    "mx-auto",
    "px-4"
  ).mkString(" ")

  val navLinkCls = Seq(
    "text-site-muted",
    "hover:text-site-text",
    "text-lg",
    "no-underline"
  ).mkString(" ")

  val albumRowCls = Seq(
    "max-w-[1200px]",
    "mx-auto",
    "px-2",
    "mb-0.5"
  ).mkString(" ")

  val imgCls = Seq(
    "w-full",
    "block",
    "cursor-pointer"
  ).mkString(" ")

  val overlayNavCls = Seq(
    "absolute",
    "top-0",
    "bottom-0",
    "w-[12%]",
    "flex",
    "items-center",
    "justify-center",
    "text-white/60",
    "hover:text-white/90",
    "hover:bg-white/5",
    "cursor-pointer",
    "select-none",
    "z-10"
  ).mkString(" ")
}
