package startup


/** Singleton  Object twStat which hold all the total information capture by the Tw_Collector
  *
  *  @constructor
  */
object twStat {

  // tw total
  var Twitter_Total : Long = 1

  // Emoji Total
  var Emoji_Total : Long = 1

  // Hash total
  var HashTag_Total : Long = 1

  // URL total
  var URL_Total : Long = 1

  // Domain total
  var Domain_Total : Long = 1

  // Photo Total
  var Photo_Total : Long = 1

  // Percent of tweets that contains emojis
  var Emoji_Percent    : String = _
  var HashTag_Percent  : String = _
  var URL_Percent      : String = _
  var Photo_Percent    : String = _


  // total time
  var twTime : Double = _

  var twPerSecond  : Double = _
  var twPerMinutes : Double = _
  var twPerHour    : Double = _


  var twTimeUnit : String = "sec"



  /** Method  Use for Compute the Percent
    *
    */
  def Percent : Unit = {

    // Emoji Percent
    Emoji_Percent =  s"${Math.round((Emoji_Total.toDouble / Twitter_Total.toDouble) * 100.0.toDouble)} %"

    // Percent HagTag / Tw
    HashTag_Percent =  s"${Math.round((HashTag_Total.toDouble / Twitter_Total.toDouble) * 100.0.toDouble)} %"

    URL_Percent     =  s"${Math.round((URL_Total.toDouble / Twitter_Total.toDouble) * 100.0.toDouble)} %"

    Photo_Percent   =  s"${Math.round((Photo_Total.toDouble / Twitter_Total.toDouble) * 100.0.toDouble)} %"

    twPerSecond     =  Math.round((Twitter_Total.toDouble / twTime.toDouble))
    twPerMinutes    =  Math.round((Twitter_Total.toDouble / (twTime.toDouble / 60)))
    twPerHour       =  Math.round((Twitter_Total.toDouble / (twTime.toDouble / 3600)))

  }



  /** Method  Use Initialize Data
    *
    */
  def init : Unit = {
    Twitter_Total = 0 ; Emoji_Total = 0 ; HashTag_Total = 0 ;  URL_Total = 0 ; Domain_Total = 0 ; Photo_Total  = 0
        twTime = 0
  }

}


/** Case Class twStatus use for Reporting the information back to the user. It use for be convert to JSON
  *
  *  @constructor create a new twStatus with code and msg
  *  @param code the code name
  *  @param msg message description for the code
  */
case class  twStatus (code : String, msg : String)
