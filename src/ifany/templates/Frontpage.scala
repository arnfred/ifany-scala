package ifany

import scalatags.Text.all.*
import HtmxAttrs.*

object FrontpageTemplate {

  def apply(view: FrontpageView, session: Option[Session]): String = {
    Base.page(view, session, body = frag(
      headerSection(view),
      galleries(view)
    ))
  }

  private def headerSection(view: FrontpageView): Frag = {
    val coverSrcset = (for (label <- view.cover.image.versions)
      yield s"${view.cover.image.imageURL(view.cover.album.url, label)} ${view.cover.image.width(label)}w"
    ).mkString(", ")

    div(cls := s"$twoColLayout pt-8",
      div(cls := "sm:col-start-2",
        h1(cls := Seq(
          "text-[min(9rem,18vw)]",
          "sm:text-[min(9rem,12vw)]",
          "font-bold",
          "leading-none",
          "tracking-tight",
          "-ml-1"
        ).mkString(" "),
          span(cls := "text-site-muted", "if"),
          span(cls := "text-site-secondary", "any")
        ),
        h4(cls := "text-site-muted -mt-[0.6em] mb-6 text-2xl font-medium ml-0.5", "photography"),
        p(cls := "mt-12 text-sm",
          raw("""The photos on this site are an ongoing
          collection of things, people and places that happen to stand in my
          way the moment I press the shutter. Check out the meta albums of
          <a href="/all/1" class="text-site-link hover:text-site-link-hover">all</a>,
          <a href="/random" class="text-site-link hover:text-site-link-hover">random</a> and
          <a href="/covers" class="text-site-link hover:text-site-link-hover">cover</a>
          photos. Inquiries and fawning fan mail are
          all welcome at <a href="mailto:jonas@ifany.org" class="text-site-link hover:text-site-link-hover">jonas@ifany.org</a>.""")
        ),
        p(cls := "text-right mt-2",
          a(href := "mailto:jonas@ifany.org", cls := Seq(
            "text-site-link",
            "hover:text-site-link-hover"
          ).mkString(" "), "Jonas Arnfred")
        )
      ),
      div(cls := "sm:col-start-3 hidden sm:block",
        img(
          src := view.cover.image.imageURL(view.cover.album.url, "800"),
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
          raw(s"""From the album "<a href="${view.cover.album.path}/" class="text-site-link hover:text-site-link-hover">${view.cover.album.title}</a>"""")
        )
      )
    )
  }

  private def galleries(view: FrontpageView): Frag =
    frag(for (g <- view.getGalleries) yield {
      val cover = view.getGalleryCover(g)
      galleryAccordion(view, g, cover)
    })

  private def galleryAccordion(view: FrontpageView, g: Gallery, cover: Cover): Frag = {
    val albumNum = g.albums.size
    val imagesNum = view.getGallerySize(g)

    tag("details")(cls := "group",
      attr("ontoggle") := "if(this.open) this.scrollIntoView({behavior:'smooth', block:'start'})",
      tag("summary")(cls := "cursor-pointer list-none",
        div(cls := Seq(
          twoColLayout,
          "py-4",
          "hover:bg-site-hover-bg",
          "transition-colors"
        ).mkString(" "),
          div(cls := "sm:col-start-2",
            img(
              src := cover.image.imageURL(cover.album.url, "s"),
              cls := Seq(
                "w-full",
                "aspect-[5/3]",
                "object-cover",
                "object-center"
              ).mkString(" "),
              loading := "lazy"
            )
          ),
          div(cls := "sm:col-start-3",
            h2(cls := "text-2xl leading-tight", g.name),
            p(cls := "inline text-sm", view.getGalleryDateString(g), "."),
            p(cls := "inline italic text-sm",
              s" This gallery contains $albumNum",
              if (albumNum == 1) " album" else " albums",
              s" with $imagesNum images.",
            ),
            p(cls := "mt-1 text-sm", raw(g.description))
          )
        )
      ),
      div(cls := Seq(
        "grid",
        "grid-rows-[0fr]",
        "group-open:grid-rows-[1fr]",
        "transition-[grid-template-rows]",
        "duration-300",
        "ease-in-out",
        "overflow-hidden"
      ).mkString(" "),
        div(cls := "overflow-hidden",
          for (album <- g.albums.reverse) yield albumRow(view, g.url, album)
        )
      )
    )
  }
}
