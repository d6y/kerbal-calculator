package com.github.fommil.kerbal

import utest._

object SolverSuite extends utest.TestSuite {

  private def bestEngines(solns: Stream[EngineSolution]): Set[String] =
    solns.sortBy(_.stageInitialMass).take(20).map(_.engine.name).toSet

  private def similar(a: Double, b: Double, e: Double = 1e-06): Boolean = (a >= (b - e)) && (a <= (b + e))

  def tests = TestSuite {
    "should calculate the correct deltav for a mainsail with orange tank"-{
      // http://forum.kerbalspaceprogram.com/threads/107592-web-calculator-for-engines-fuel
      val mainsail = Engines.Stock.engines.find(_.name.contains("Mainsail")).get
      val orange = FuelTanks.Stock.tanks.find(_.name.contains("Jumbo-64")).get
      val dv = EngineSolution(11, mainsail, 1, orange, 32, false, Nil).totalDeltaV

      // 360 * 9.81 * math.log((11 + 6 + 36.0) / (11 + 6 + 4))
      assert(similar(dv, 3269.447, 1.0))
    }

    "should recommend sensible engines for a 10t payload from Kerbin to the Mun"-{
      val results = Solver.solve(1200, 10, 20, false, Large)
      val engines = bestEngines(results)
      val expect = Set(
        "RT-10 (Radial)", // a bit weird
        "Rockomax BACC (Radial)", // also weird
        "Rockomax \"Skipper\"",
        "S1 SRB-KD25k",
        "Rockomax Mark 55"
      )
      assert(engines == expect)
    }

    "should recommend sensible engines for a long-burn small satellite"-{
      val results = Solver.solve(1000, 1, 1, false, Tiny)
      val engines = bestEngines(results)
      val expect = Set(
        "PB-ION",
        "LV-1",
        "Rockomax 48-7S", // weird
        "LV-1R"
      )
      assert(engines == expect)
    }

    "should recommend radial solid boosters and lifters for a lift-off"-{
      val results = Solver.solve(1000, 50, 10, true, Large)
      val engines = bestEngines(results)
      val expect = Set(
        "Kerbodyne KR-2L",
        "Rockomax \"Mainsail\"",
        "S3 KS-25x4 Cluster",
        "S1 SRB-KD25k (Radial)",
        "LFB KR-1x2"
      )
      assert(engines == expect)
    }
  }
}
