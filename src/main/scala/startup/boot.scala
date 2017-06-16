package startup

import com.google.gson.GsonBuilder
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.slf4j.LoggerFactory
import spark.Spark.{get, port}
import spark.{Request, Response}
import sun.misc.{Signal, SignalHandler}


/**
  * Created by user on 6/12/17.
  */
object boot extends App with SignalHandler {


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
  logger.info(s"Twitter REST Server Version 1.0.0 No. of Processor ${procs}")

  //--------------------------------------------------------------------------------------------------------------------
  // Configuration
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



  //--------------------------------------------------------------------------------------------------------------------
  // Method for Handling the Operating Signal - shutdown
  //--------------------------------------------------------------------------------------------------------------------
  override def handle(signal: Signal): Unit = {

    logger.info(s"Shutdown Twitter REST Server...")
    // Wait for the Close of the Cluster
    System.exit(0)
  }

  def getTwVersion (req : Request, res : Response) : String = {
    logger.info (s"Request initTw ----> ")
    ggson.toJson(twStatus ("200", "Tw-Rest - Version 1.0.0"))
  }


  def initTw (req : Request, res : Response) : String = {

    logger.info (s"Request initTw ----> ")

    // Initialize the Stat.
    twStat.init

    // Response Status Messages
    ggson.toJson(twStatus ("200", "Success"))


  }

  def getTwTags (req : Request, res : Response) : String = {

    logger.info (s"Request getTwTags ----> ")

    //--------------------------------------------------------------------------------------------------------------------
    // Get the Data From Grid
    //--------------------------------------------------------------------------------------------------------------------
    val cacheHashTag        = ignite.cache [String,Long] ("hashTag")

    var tags = new java.util.HashMap [String,Long] ()
    cacheHashTag.forEach(item => {
      tags.put(item.getKey, item.getValue.toLong)
         System.out.println (s" Key ${item.getKey} Value ${item.getValue} ")

    })

    //--------------------------------------------------------------------------------------------------------------------
    // Response Status Messages
    //--------------------------------------------------------------------------------------------------------------------
    ggson.toJson(tags)


  }


  def getTwEmoji (req : Request, res : Response) : String = {

    logger.info (s"Request getTwEmoji ----> ")

    //--------------------------------------------------------------------------------------------------------------------
    // Get the Data From Grid
    //--------------------------------------------------------------------------------------------------------------------
    val emojitop        = ignite.cache [String,Long] ("emojiTop")


    var tags = new java.util.HashMap [String,Long] ()
    emojitop.forEach(item => {
      tags.put(item.getKey, item.getValue.toLong)
      System.out.println (s" Key ${item.getKey} Value ${item.getValue} ")

    })

    //--------------------------------------------------------------------------------------------------------------------
    // Response Status Messages
    //--------------------------------------------------------------------------------------------------------------------
    ggson.toJson(tags)

  }

  def getTwDomainTop (req : Request, res : Response) : String = {

    logger.info (s"Request getTwDomainTop ----> ")

    //--------------------------------------------------------------------------------------------------------------------
    // Get the Data From Grid
    //--------------------------------------------------------------------------------------------------------------------
    val DomainTopIgnite              = ignite.getOrCreateCache [String,Long] ("DomainTop")

    var dtop = new java.util.HashMap [String,Long] ()
    DomainTopIgnite.forEach(item => {
      dtop.put(item.getKey, item.getValue.toLong)
      System.out.println (s" Key ${item.getKey} Value ${item.getValue} ")

    })

    //--------------------------------------------------------------------------------------------------------------------
    // Response Status Messages
    //--------------------------------------------------------------------------------------------------------------------
    ggson.toJson(dtop)

  }

  def getTwURLTop (req : Request, res : Response) : String = {

    logger.info (s"Request getTwURLTop ----> ")

    //--------------------------------------------------------------------------------------------------------------------
    // Get the Data From Grid
    //--------------------------------------------------------------------------------------------------------------------
    val URLTopIgnite                 = ignite.getOrCreateCache [String,Long] ("URLTop")

    var urltop = new java.util.HashMap [String,Long] ()
    URLTopIgnite.forEach(item => {
      urltop.put(item.getKey, item.getValue.toLong)
      System.out.println (s" Key ${item.getKey} Value ${item.getValue} ")

    })

    //--------------------------------------------------------------------------------------------------------------------
    // Response Status Messages
    //--------------------------------------------------------------------------------------------------------------------
    ggson.toJson(urltop)

  }

  def getPhotoTop (req : Request, res : Response) : String = {

    logger.info (s"Request getPhotoTop ----> ")

    //--------------------------------------------------------------------------------------------------------------------
    // Get the Data From Grid
    //--------------------------------------------------------------------------------------------------------------------
    val PhotoIgnite       = ignite.getOrCreateCache [String,Long] ("PhotoTag")

    var phototop = new java.util.HashMap [String,Long] ()
    PhotoIgnite.forEach(item => {
      phototop.put(item.getKey, item.getValue.toLong)
      System.out.println (s" Key ${item.getKey} Value ${item.getValue} ")

    })

    //--------------------------------------------------------------------------------------------------------------------
    // Response Status Messages
    //--------------------------------------------------------------------------------------------------------------------
    ggson.toJson(phototop)

  }

  //--------------------------------------------------------------------------------------------------------------------
  // Methods to support the Rest EndPoint
  //--------------------------------------------------------------------------------------------------------------------
  def getTwTotals (req : Request, res : Response) : String = {


          logger.info (s"Request getTwInfo ----> ")



          //-----------------------------------------------------------------------------------------------------------
          // Get the Data From Grid
          //-----------------------------------------------------------------------------------------------------------

          val TotalIgnite = ignite.getOrCreateCache [String,Long] ("Total")

          TotalIgnite.forEach(data => {

              println(s"Key ${data.getKey} Value ${data.getValue} ")

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
