// Lightbox module — vanilla JS, no dependencies
// Manages full-screen image overlay with keyboard/click navigation

const lightbox = {
  overlay: null,
  images: [],
  currentIndex: -1,
  albumUrl: '',

  init(albumData) {
    this.images = albumData.images
    this.albumUrl = albumData.url
    this.overlay = document.getElementById('overlay')
    this.bindEvents()
    this.initVideoObserver()
    this.openFromURL()
  },

  bindEvents() {
    // Click on any .media image → open overlay
    document.querySelectorAll('img.media').forEach(el => {
      el.addEventListener('click', () => this.open(el.getAttribute('file')))
    })

    // Keyboard navigation
    document.addEventListener('keydown', (e) => {
      if (!this.isOpen()) return
      if (e.key === 'ArrowLeft' || e.key === 'Backspace') { e.preventDefault(); this.prev() }
      else if (e.key === 'ArrowRight' || e.key === ' ' || e.key === 'Enter') { e.preventDefault(); this.next() }
      else if (e.key === 'Escape') this.close()
    })

    // Click overlay background to close
    document.getElementById('overlay-img').addEventListener('click', (e) => {
      if (e.target.id === 'overlay-img') this.close()
    })
    document.getElementById('overlay-prev').addEventListener('click', () => this.prev())
    document.getElementById('overlay-next').addEventListener('click', () => this.next())
  },

  open(filename) {
    const index = this.images.findIndex(img => img.file === filename)
    if (index === -1) return
    this.currentIndex = index
    this.render()
    this.overlay.classList.remove('hidden')
    this.overlay.classList.add('flex')
    document.body.style.overflowY = 'hidden'
    history.replaceState({}, '', this.baseURL() + filename)
  },

  close() {
    this.overlay.classList.add('hidden')
    this.overlay.classList.remove('flex')
    document.body.style.overflowY = 'auto'
    this.currentIndex = -1
    history.replaceState({}, '', this.baseURL())
  },

  next() {
    if (this.currentIndex < this.images.length - 1)
      this.open(this.images[this.currentIndex + 1].file)
  },

  prev() {
    if (this.currentIndex > 0)
      this.open(this.images[this.currentIndex - 1].file)
  },

  isOpen() { return this.currentIndex >= 0 },

  render() {
    const img = this.images[this.currentIndex]
    const container = document.getElementById('overlay-center-box')
    const fileId = 'id-' + img.file.replaceAll('/', '--').replaceAll('.', '--')
    const sourceEl = document.getElementById(fileId)

    // Remove existing media
    const oldMedia = container.querySelector('.media')
    if (oldMedia) oldMedia.remove()

    if (sourceEl) {
      const clone = sourceEl.cloneNode(true)
      clone.removeAttribute('id')
      clone.style.width = ''
      clone.classList.add('max-h-screen')
      if (clone.tagName === 'VIDEO') {
        clone.preload = 'auto'
        clone.controls = true
      }
      container.prepend(clone)
    }

    document.getElementById('caption').textContent = img.description || ''

    // Show/hide nav arrows
    document.getElementById('overlay-prev').style.visibility = this.currentIndex > 0 ? '' : 'hidden'
    document.getElementById('overlay-next').style.visibility = this.currentIndex < this.images.length - 1 ? '' : 'hidden'

    this.resizeOverlay()
    this.prefetchAdjacent()
  },

  // Prefetch prev and next images by triggering browser cache via hidden Image objects
  prefetchAdjacent() {
    for (const offset of [-1, 1]) {
      const idx = this.currentIndex + offset
      if (idx < 0 || idx >= this.images.length) continue
      const adjImg = this.images[idx]
      if (adjImg.is_video) continue
      const fileId = 'id-' + adjImg.file.replaceAll('/', '--').replaceAll('.', '--')
      const el = document.getElementById(fileId)
      if (el && el.complete) continue
      const prefetch = new Image()
      prefetch.srcset = el ? el.getAttribute('srcset') || '' : ''
      prefetch.sizes = '96vw'
      if (!prefetch.srcset && el) prefetch.src = el.src
    }
  },

  resizeOverlay() {
    const media = this.overlay.querySelector('#overlay-center-box .media')
    if (!media) return
    const onReady = () => {
      const w = media.naturalWidth || media.videoWidth
      const h = media.naturalHeight || media.videoHeight
      if (!w || !h) return
      const vpRatio = (window.innerWidth * 0.96) / window.innerHeight
      const imgRatio = w / h
      const pct = imgRatio >= vpRatio ? '100%' : (imgRatio / vpRatio * 100) + '%'
      media.style.width = pct
    }
    media.addEventListener('load', onReady, { once: true })
    media.addEventListener('loadedmetadata', onReady, { once: true })
    if (media.complete) onReady()
  },

  baseURL() {
    const parts = window.location.pathname.split('/')
    // URL pattern: /gallery/album/ or /gallery/album/image
    return '/' + parts[1] + '/' + parts[2] + '/'
  },

  openFromURL() {
    const parts = window.location.pathname.split('/').filter(Boolean)
    if (parts.length >= 3) {
      const filename = parts.slice(2).join('/')
      if (this.images.some(img => img.file === filename)) {
        this.open(filename)
      }
    }
  },

  // Lazy-preload videos when they scroll into view
  initVideoObserver() {
    if (!('IntersectionObserver' in window)) return
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(e => {
        if (e.isIntersecting) {
          e.target.preload = 'auto'
          observer.unobserve(e.target)
        }
      })
    })
    document.querySelectorAll('video').forEach(v => observer.observe(v))
  }
}

export default lightbox
