package editor

import scala.scalajs.js
import scala.scalajs.js.`import`
import scala.scalajs.js.JSON

import com.raquo.laminar.api.L.{*, given}
import com.raquo.waypoint.*

object Routes:
  enum Page(val title: String):
    case Home extends Page("Home")
    case SignalObserver extends Page("SignalObserver")
    case DerivedVar extends Page("DerivedVar")
    case Updater extends Page("Updater")
    case GraphEditor extends Page("GraphEditor")
    case Flowbite extends Page("Flowbite")

  private def basePath: String =
    `import`.meta.env.BASE_URL.asInstanceOf[String] + "#"

  private val routes = List(
    Route.static(Page.Home, root / endOfSegments, basePath),
    Route.static(Page.SignalObserver, root / "signal-observer" / endOfSegments, basePath),
    Route.static(Page.DerivedVar, root / "derived-var" / endOfSegments, basePath),
    Route.static(Page.Updater, root / "updater" / endOfSegments, basePath),
    Route.static(Page.GraphEditor, root / "editor" / endOfSegments, basePath),
    Route.static(Page.Flowbite, root / "flowbite" / endOfSegments, basePath),
  )

  val router = new Router[Page](
    routes = routes,
    getPageTitle = _.title.asInstanceOf[String],
    serializePage = _.title,
    deserializePage = Page.valueOf(_)
  )(
    popStateEvents = windowEvents(_.onPopState),
    owner = unsafeWindowOwner
  )
