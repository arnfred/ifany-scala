# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Scala backend for www.ifany.org — a photography portfolio/gallery website. Serves photos and videos stored in AWS S3 with metadata in DynamoDB.

## Build & Run

Requires: decrypted AWS credentials + Kinde auth env vars. Tools (JDK 21, SBT, Tailwind CSS) are managed by `mise`.

```bash
# Setup (one-time)
mise install                     # installs java 21, sbt, tailwindcss
gpg --decrypt < aws.test.env.gpg > aws.test.env

# Compile
sbt compile

# Dev server with auto-restart + Tailwind watch (test env)
mise run dev

# Dev server with prod env
mise run dev:prod

# Build Tailwind CSS (after template changes)
mise run css

# Package for deployment
sbt compile stage

# Deploy to production
git push dynkarken master

# Push to GitHub
git push origin master
```

Inside the SBT shell, use `run` or `compile` directly.

There are no tests in this project.

## Architecture

**Stack:** Scala 3.6.4 + Unfiltered (Netty HTTP) + ScalaTags 0.13.1 + Tailwind CSS v4 + htmx 2.0 + JSON4s + AWScala (S3/DynamoDB) + jwt-json4s-native (JWT parsing)

**Request flow:** Route matching → Auth check → Model fetch → View construction → ScalaTags template → HTML string

```
plans/Gallery.scala    — Single route handler (Unfiltered intents), all URL routing + auth routes
models/                — Domain models + DynamoDB access (Album, Gallery, Frontpage, Navigation)
views/                 — View models that structure data for display
templates/             — ScalaTags HTML generation; Base.scala wraps pages in HTML5 shell
HtmxAttrs.scala        — Shared htmx/HTML attributes + reusable Tailwind class vals
Auth.scala             — Session cookie (HMAC-SHA256) + Kinde OAuth2 client
Ifany.scala            — Server initialization + Main entry point
s3.scala               — S3 presigned URL generation (60-min expiry)
```

**Key routes** (all in `plans/Gallery.scala`):
- `/` frontpage, `/<gallery>` gallery view, `/<gallery>/<album>` album view
- `/<gallery>/<album>/<password>` password-protected albums (SHA-256 hash)
- `/covers` random covers, `/all/<page>` paginated, `/videos` all videos, `/random` 100 random
- `/login` login page, `/auth/login` redirect to Kinde, `/auth/callback` OAuth2 callback, `/auth/logout` logout

**Authentication** (Kinde OAuth2):
- Albums with `datetime._2 >= 2020-11-25` require the `private/access` permission
- `/update` route requires the `Admin` role
- Session stored in HMAC-SHA256 signed cookie (`ifany_session`, 30-day expiry)
- Auth routes are in `GalleryPlan` (not a separate handler) because Unfiltered Netty doesn't reliably chain async plan handlers — the catch-all album pattern would match `/auth/callback` etc.
- `Auth.scala` contains: `Session` case class (email, roles, permissions, expiry), `SessionCookie` object (encode/decode/sign/verify), `KindeClient` object (authorization URL, token exchange, role/permission extraction from access token)

**Data layer:** No local database. Albums/galleries cached in-memory after first DynamoDB fetch. `Album.get()` and `Gallery.get()` are the primary data accessors. Environment variables configure bucket/table names (`IMAGES_BUCKET`, `ALBUMS_TABLE`, `GALLERIES_TABLE`).

**Frontend:** Tailwind CSS v4 for styling, htmx 2.0 loaded on every page (for future edit mode), vanilla JS lightbox (`resources/js/lightbox.js`) for album image overlay. Album JSON data is injected into the page via `AlbumView.getJson` using json4s JsonDSL (not reflection-based serialization, which is incompatible with Scala 3). Compiled Tailwind CSS is committed as `resources/css/site.css` — regenerate with `mise run css` after template changes.

## Templates (ScalaTags)

Templates are Scala objects in `src/ifany/templates/` that return `String` via ScalaTags `.render`:

- **`Base.page(view, session, headerExtra, body)`** — wraps content in HTML5 shell with `<head>`, footer, session login/logout
- **`FrontpageTemplate(view, session)`** — gallery accordion using `<details>/<summary>` with CSS animation
- **`GalleryTemplate(view, session)`** — album list with navigation
- **`AlbumTemplate(view, session)`** — image rows (CoverRow/DualRow) + lightbox overlay
- **`LoginRequiredTemplate(albumPath)`** — login prompt
- **`PendingApprovalTemplate(email)`** — pending approval message

**ScalaTags conventions:**
- Use `scalatags.Text.tags2.title` for the `<title>` tag (conflicts with `title` attribute)
- `crossorigin` is already in `scalatags.Text.all` — do NOT redefine it in `HtmxAttrs`
- Custom attributes (htmx, `loading`, `srcset`, `sizes`, `poster`, `playsinline`, `preload`) are defined in `HtmxAttrs.scala`
- Shared Tailwind class strings (`twoColLayout`, `albumRowCls`, `navLinkCls`, etc.) are vals in `HtmxAttrs`
- Use `raw()` only for trusted HTML content (album descriptions with links, gallery HTML)
- DualRow equal-height: `flex-grow` set to aspect ratio — flexbox distributes width so heights match
- **Tailwind class lists** must use `Seq(...).mkString(" ")` with one class per line:
  ```scala
  cls := Seq(
    "flex",
    "items-center",
    "mt-4"
  ).mkString(" ")
  ```

**Lightbox** (`resources/js/lightbox.js`):
- Vanilla JS ES module, no dependencies
- Clones `<img>` elements from the DOM (reuses browser-cached srcset)
- Keyboard nav (arrows, escape, space), click nav, deep linking via `history.replaceState`
- `prefetchAdjacent()` preloads prev/next images for instant arrow navigation
- `IntersectionObserver` for video lazy preloading
- Videos play on hover via inline `onmouseover`/`onmouseleave` attributes

## Environment Variables

**AWS:** `IMAGES_BUCKET`, `ALBUMS_TABLE`, `GALLERIES_TABLE`, plus AWS credentials.

**Auth:**
| Variable | Description | Example |
|---|---|---|
| `KINDE_DOMAIN` | Kinde tenant URL | `https://ifany.kinde.com` |
| `KINDE_CLIENT_ID` | Kinde app client ID | From Kinde dashboard |
| `KINDE_CLIENT_SECRET` | Kinde app client secret | From Kinde dashboard |
| `KINDE_REDIRECT_URI` | OAuth2 callback URL | `https://www.ifany.org/auth/callback` (prod) / `http://localhost:8000/auth/callback` (dev) |
| `SESSION_SECRET` | Cookie signing secret | Random 32+ char string |

Production env vars are set via `dokku config:set photos KEY=VALUE`.

## Deployment

- **Production remote:** `dynkarken` → `dokku@dynkarken.com:photos`
- **GitHub remote:** `origin` → `git@github.com:arnfred/ifany-scala.git`
- Dokku runs on dynkarken.com, serves via nginx at `www.ifany.org`
- Deploy with `git push dynkarken master` (triggers heroku buildpack build)
- Compiled `resources/css/site.css` is committed to the repo so Dokku builds don't need the Tailwind CLI

## Conventions

- Scala source lives in `src/ifany/` (not the standard `src/main/scala/` layout)
- Scala 3 syntax: `given`/`using` instead of `implicit`, `*` wildcard imports instead of `_`
- Models use companion objects with caching (Option-based in-memory cache)
- Custom exceptions in `Errors.scala`: `InternalError`, `AlbumNotFound`, `GalleryNotFound`
- Templates accept `session: Option[Session]` as a required parameter (no default) to show login/logout in footer
- Do NOT use json4s reflection-based serialization (`Serialization.write`) — it relies on Scala 2 `ScalaSig` which doesn't exist in Scala 3. Use `JsonDSL` + `compact(render(...))` instead.
- Tool versions managed via `mise.toml` (java 21, sbt 1.9.7, tailwindcss 4.2.1)

## Known Issues

- S3 test bucket (`ifany.images.test`) needs CORS configured for local dev: `aws s3api put-bucket-cors --bucket ifany.images.test --cors-configuration file://cors.json`
- json4s deprecation warnings about `Manifest` synthesis in Scala 3 (in `Auth.scala` extractOpt calls) — cosmetic only, doesn't affect functionality
