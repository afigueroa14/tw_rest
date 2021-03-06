package startup

import com.google.gson.GsonBuilder
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.slf4j.LoggerFactory
import spark.Spark.{get, port}
import spark.{Request, Response}
import sun.misc.{Signal, SignalHandler}

import scala.util.Try


/** Singleton Application Object Twitter Rest Services Application. Entry Point for the application
  *
  *  @author angel Figueroa Cruz
  *  @version 1.0.0
  *  @constructor
  */
object boot extends App with SignalHandler {

  //--------------------------------------------------------------------------------------------------------------------
  // Vertion of the Application
  //--------------------------------------------------------------------------------------------------------------------
  val Version = "1.0.0"

  //--------------------------------------------------------------------------------------------------------------------
  // Calculate the number of processors
  //--------------------------------------------------------------------------------------------------------------------
  val procs = Runtime.getRuntime.availableProcessors

  //--------------------------------------------------------------------------------------------------------------------
  // method use for processing the signal
  //--------------------------------------------------------------------------------------------------------------------
  val SIGING = "INT"
  val SIGTERM = "TERM"
  Signal.handle(new Signal(SIGING), this)
  Signal.handle(new Signal(SIGTERM), this)

  //--------------------------------------------------------------------------------------------------------------------
  // Ignite configuration.
  //--------------------------------------------------------------------------------------------------------------------
  val igniteCfg = new IgniteConfiguration
  val ignite = Ignition.start(igniteCfg)

  //------------------------------------------------------------------------------------------------------------
  // Google Gson operation
  //------------------------------------------------------------------------------------------------------------
  private val ggson = new GsonBuilder().disableHtmlEscaping.serializeNulls.create

  //--------------------------------------------------------------------------------------------------------------------
  // Log
  //--------------------------------------------------------------------------------------------------------------------
  val logger = LoggerFactory. getLogger("boot")
  logger.info(s"Twitter REST Server Version ${Version} No. of Processor ${procs}")

  //--------------------------------------------------------------------------------------------------------------------
  // HTTP Port Configuration
  //--------------------------------------------------------------------------------------------------------------------
   val ServerPort = 8090

  //--------------------------------------------------------------------------------------------------------------------
  // Setup the Port for the Server
  //--------------------------------------------------------------------------------------------------------------------
  port(ServerPort) // Spark will run on port 9090

  //--------------------------------------------------------------------------------------------------------------------
  // Setup the EndPoint
  //--------------------------------------------------------------------------------------------------------------------
  get("/tw/version"    , (request: Request, response: Response) => getTwVersion     (request, response))
  get("/tw/totals"     , (request: Request, response: Response) => getTwTotals      (request, response))
  get("/tw/hashtags"   , (request: Request, response: Response) => getTwTags        (request, response))
  get("/tw/emoji"      , (request: Request, response: Response) => getTwEmoji       (request, response))
  get("/tw/domaintop"  , (request: Request, response: Response) => getTwDomainTop   (request, response))
  get("/tw/urltop"     , (request: Request, response: Response) => getTwURLTop      (request, response))
  get("/tw/photo"      , (request: Request, response: Response) => getPhotoTop      (request, response))
  get("/tw/init"       , (request: Request, response: Response) => initTw           (request, response))

  // Capture any Route not processed
  spark.Spark.notFound ((req: Request, res: Response) => InvalidPath (req,res))

  // Internal Server Error
  spark.Spark.internalServerError((req: Request, res: Response) => {
    def InternalError (req: Request, res: Response) : String = {
      res.`type`("application/json")
      ggson.toJson(twStatus(500, "Internal Error..."))
    }

    InternalError(req, res)
  })

  /** Method  Method for Handling the Operating Signal - shutdown
    *
    *  @param signal Signal of the OS
    */
  override def handle(signal: Signal): Unit = {

    logger.info(s"Shutdown Twitter REST Server...")
    // Wait for the Close of the Cluster
    System.exit(0)
  }


  /** Method  Processing Invalid HTTP EntPoint
    *
    *  @param req the HTTP Request
    *  @param res the HTTP Response
    */
  def InvalidPath (req : Request, res : Response) : String = {

    logger.info(String.format ("Invalid Path %s ",req.pathInfo()));

    res.status(400);
    ggson.toJson(twStatus (400,"Invalid Route"))

  }

  /** Method  Processing the HTTP EntPoint https://{ServerOP}:{Port}/tw/version <- Get the Versiotn of the Application
    *
    *  @param req the HTTP Request
    *  @param res the HTTP Response
    */
  def getTwVersion (req : Request, res : Response) : String = {
    logger.info (s"Request initTw ----> ")
    ggson.toJson(twStatus (200, s"Tw-Rest - Version ${Version}"))
  }


  /** Method  Processing the HTTP EntPoint https://{ServerOP}:{Port}/tw/init <- Initialization of Data
    *
    *  @param req the HTTP Request
    *  @param res the HTTP Response
    */
  def initTw (req : Request, res : Response) : String = {

    logger.info (s"Request initTw ----> ")

    // Initialize the Stat.
    twStat.init

    // Response Status Messages
    ggson.toJson(twStatus (200, "Success"))


  }

  /** Method  Processing the HTTP EntPoint https://{ServerOP}:{Port}/tw/hashtags <- Generate a List with all Tags capture by tw_collector
    *
    *  @param req the HTTP Request
    *  @param res the HTTP Response
    */
  def getTwTags (req : Request, res : Response) : String = {


    //---------------------------------------------------------------------
    // Get the Parameter
    //---------------------------------------------------------------------
    val maxvalue = Try(req.queryParams("maxvalue").toInt).getOrElse(-1)

    logger.info (s"Request getTwTags ----> maxvalue ${maxvalue}")


    //--------------------------------------------------------------------------------------------------------------------
    // Get the Data From Grid
    //--------------------------------------------------------------------------------------------------------------------
    val cacheHashTag        = ignite.getOrCreateCache [String,Long] ("hashTag")

    var tags = new java.util.HashMap [String,Long] ()
    cacheHashTag.forEach(item => {

      // Check if Condition value
      if (maxvalue > 0) {
          if (item.getValue >= maxvalue) tags.put(item.getKey, item.getValue)
      }
      else tags.put(item.getKey, item.getValue)

    })


    //--------------------------------------------------------------------------------------------------------------------
    // Response Status Messages
    //--------------------------------------------------------------------------------------------------------------------
    ggson.toJson(tags)


  }

  /** Method  Processing the HTTP EntPoint https://{ServerOP}:{Port}/tw/emoji <- Generate a List with all Emoji capture by tw_collector
    *
    *  @param req the HTTP Request
    *  @param res the HTTP Response
    */
  def getTwEmoji (req : Request, res : Response) : String = {

    //---------------------------------------------------------------------
    // Get the Parameter
    //---------------------------------------------------------------------
    val maxvalue = Try(req.queryParams("maxvalue").toInt).getOrElse(-1)

    logger.info (s"Request getTwEmoji ----> maxvalue ${maxvalue}")

    //--------------------------------------------------------------------------------------------------------------------
    // Get the Data From Grid
    //--------------------------------------------------------------------------------------------------------------------
    val emojitop        = ignite.getOrCreateCache [String,Long] ("emojiTop")

    var emoji = new java.util.HashMap [String,Long] ()

    emojitop.forEach(item => {

      logger.info(s" --> ${item.getKey} === ${item.getValue}")

      // Check if Condition value
      if (maxvalue > 0) {
        if (item.getValue >= maxvalue) emoji.put(item.getKey, item.getValue)
      }
      else emoji.put(item.getKey, item.getValue)

    })


    //--------------------------------------------------------------------------------------------------------------------
    // Response Status Messages
    //--------------------------------------------------------------------------------------------------------------------
    ggson.toJson(emoji)

  }



  /** Method  Processing the HTTP EntPoint https://{ServerOP}:{Port}/tw/domaintop <- Generate a List with all Domain capture by tw_collector
    *
    *  @param req the HTTP Request
    *  @param res the HTTP Response
    */
  def getTwDomainTop (req : Request, res : Response) : String = {


    //---------------------------------------------------------------------
    // Get the Parameter
    //---------------------------------------------------------------------
    val maxvalue = Try(req.queryParams("maxvalue").toInt).getOrElse(-1)

    logger.info (s"Request getTwDomainTop ----> maxvalue ${maxvalue}")

    //--------------------------------------------------------------------------------------------------------------------
    // Get the Data From Grid
    //--------------------------------------------------------------------------------------------------------------------
    val DomainTopIgnite              = ignite.getOrCreateCache [String,Long] ("DomainTop")

    var dtop = new java.util.HashMap [String,Long] ()
    DomainTopIgnite.forEach(item => {

      // Check if Condition value
      if (maxvalue > 0) {
        if (item.getValue >= maxvalue) dtop.put(item.getKey, item.getValue)
      }
      else dtop.put(item.getKey, item.getValue)

    })

    //--------------------------------------------------------------------------------------------------------------------
    // Response Status Messages
    //--------------------------------------------------------------------------------------------------------------------
    ggson.toJson(dtop)

  }


  /** Method  Processing the HTTP EntPoint https://{ServerOP}:{Port}/tw/urltop <- Generate a List with all URL capture by tw_collector
    *
    *  @param req the HTTP Request
    *  @param res the HTTP Response
    */
  def getTwURLTop (req : Request, res : Response) : String = {


    //---------------------------------------------------------------------
    // Get the Parameter
    //---------------------------------------------------------------------
    val maxvalue = Try(req.queryParams("maxvalue").toInt).getOrElse(-1)

    logger.info (s"Request getTwURLTop ----> maxvalue ${maxvalue}")

    //--------------------------------------------------------------------------------------------------------------------
    // Get the Data From Grid
    //--------------------------------------------------------------------------------------------------------------------
    val URLTopIgnite                 = ignite.getOrCreateCache [String,Long] ("URLTop")

    var urltop = new java.util.HashMap [String,Long] ()
    URLTopIgnite.forEach(item => {

      // Check if Condition value
      if (maxvalue > 0) {
        if (item.getValue >= maxvalue) urltop.put(item.getKey, item.getValue)
      }
      else urltop.put(item.getKey, item.getValue)

    })

    //--------------------------------------------------------------------------------------------------------------------
    // Response Status Messages
    //--------------------------------------------------------------------------------------------------------------------
    ggson.toJson(urltop)

  }


  /** Method  Processing the HTTP EntPoint https://{ServerOP}:{Port}/tw/photo <- Generate a List with all Photo capture by tw_collector
    *
    *  @param req the HTTP Request
    *  @param res the HTTP Response
    */
  def getPhotoTop (req : Request, res : Response) : String = {


    //---------------------------------------------------------------------
    // Get the Parameter
    //---------------------------------------------------------------------
    val maxvalue = Try(req.queryParams("maxvalue").toInt).getOrElse(-1)

    logger.info (s"Request getPhotoTop ----> maxvalue ${maxvalue}")

    //--------------------------------------------------------------------------------------------------------------------
    // Get the Data From Grid
    //--------------------------------------------------------------------------------------------------------------------
    val PhotoIgnite       = ignite.getOrCreateCache [String,Long] ("PhotoTag")

    var phototop = new java.util.HashMap [String,Long] ()
    PhotoIgnite.forEach(item => {

      // Check if Condition value
      if (maxvalue > 0) {
        if (item.getValue >= maxvalue) phototop.put(item.getKey, item.getValue)
      }
      else phototop.put(item.getKey, item.getValue)

    })

    //--------------------------------------------------------------------------------------------------------------------
    // Response Status Messages
    //--------------------------------------------------------------------------------------------------------------------
    ggson.toJson(phototop)

  }



  /** Method  Processing the HTTP EntPoint https://{ServerOP}:{Port}/tw/totals <- Generate a List with all Total capture by tw_collector
    *
    *  @param req the HTTP Request
    *  @param res the HTTP Response
    */
  def getTwTotals (req : Request, res : Response) : String = {


          logger.info (s"Request getTwInfo ----> ")

          //-----------------------------------------------------------------------------------------------------------
          // Get the Data From Grid
          //-----------------------------------------------------------------------------------------------------------
          val TotalIgnite = ignite.getOrCreateCache [String,Long] ("Total")

          twStat.init
          TotalIgnite.forEach(data => {

              data.getKey match {
                  case "TW"      =>  twStat.Twitter_Total += data.getValue
                  case "HTAG"    =>  twStat.HashTag_Total += data.getValue
                  case "EMOJI"   =>  twStat.Emoji_Total   += data.getValue
                  case "URL"     =>  twStat.URL_Total     += data.getValue
                  case "DOMAIN"  =>  twStat.Domain_Total  += data.getValue
                  case "PHOTO"   =>  twStat.Photo_Total   += data.getValue
                  case "TI"      =>  twStat.twTime        += data.getValue
              }

          })

          // computer the Percent
          twStat.Percent

         //------------------------------------------------------------------------------------------------------------
         // Return JSON Value
         //------------------------------------------------------------------------------------------------------------
          ggson.toJson(twStat)

  }


}
