package test

import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration

/**
  * Created by user on 6/13/17.
  */
object boot_tw_emoji extends App {


  //--------------------------------------------------------------------------------------------------------------------
  // Ignite configuration.
  //--------------------------------------------------------------------------------------------------------------------
  val igniteCfg = new IgniteConfiguration
  val ignite = Ignition.start(igniteCfg)


  //--------------------------------------------------------------------------------------------------------------------
  // Get the Data From Grid
  //--------------------------------------------------------------------------------------------------------------------
  val cacheEmoji        = ignite.getOrCreateCache [String,Long] ("emojiTop")

  while (true) {

    System.out.println (s" cacheEmoji Size ${cacheEmoji.size()} ")
    if (cacheEmoji.size() > 0) {

      cacheEmoji.forEach(item => {

        System.out.println (s" Item ${item.getKey} = ${item.getValue}")

      })

    }

  }


  System.out.println (s" ...... End")
}
