package test

import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration

/**
  * Created by user on 6/14/17.
  */
object boot_tags extends App {


  //--------------------------------------------------------------------------------------------------------------------
  // Ignite configuration.
  //--------------------------------------------------------------------------------------------------------------------
  val igniteCfg = new IgniteConfiguration
  val ignite = Ignition.start(igniteCfg)

  //--------------------------------------------------------------------------------------------------------------------
  // Get the Data From Grid
  //--------------------------------------------------------------------------------------------------------------------
  val cacheHashTag        = ignite.cache [String,Long] ("hashTag")

  cacheHashTag.forEach(item => {

    System.out.println (s" Key ${item.getKey} Value ${item.getValue} ")

  })



}
