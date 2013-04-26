package LogisticRegression

import BIDMat.{Mat, FMat, DMat, IMat, CMat, BMat, CSMat, SMat, SDMat, GMat, GIMat, GSMat, HMat}
import BIDMat.MatFunctions._
import BIDMat.SciFunctions._
import BIDMat.Solvers._
import BIDMat.Plotting._

object simpleClassifier extends LoadsMat with Classifies {
  Mat.noMKL=true  //can I comment this out for better performance???
  NUM_FEATURES = 2
  def main(args:Array[String]):Unit = {
    val (examples, labels) = loadMat(args(0))
    var weights = ones(1, NUM_FEATURES)
    while ( true ) {
      for ( (x,y) <- examples zip labels ) {
        //weights += update(x, y, weights) 
        val predictions:FMat = (weights * x).t
        val z:FMat = -1 * predictions
        val logit:FMat = 1.0 /@ (1 + exp(z))
        val differences = y - logit
        println(scala.math.sqrt(sum((differences *@ differences), 1)(0,0)))
        val gradients = (x * differences).t
        weights +=  0.1f * gradients
      }
    }
  }
}

trait Classifies {
  var ALPHA = 0.1f
  def update(examples: SMat, labels: FMat, weights: FMat): FMat = {
    val predictions:FMat = (weights * examples).t
    val z:FMat = -1 * predictions
    val logit:FMat = 1.0 /@ (1 + exp(z))
    val differences = labels - logit
    println(scala.math.sqrt(sum((differences *@ differences), 1)(0,0)))
    val gradients = (examples * differences).t
    return ALPHA * gradients
  }
}


trait LoadsMat {
  var BLOCKSIZE = 1000
  var NUM_FEATURES = 100000
  def loadMat(matFile:String): Tuple2[List[SMat], List[FMat]] = {
    println("Loading Matrix")
    var labels:List[FMat] = List[FMat]()
    var examples:List[SMat] = List[SMat]()
    var ySoFar:FMat = null
    var xSoFar:SMat = null
    for ( line <- scala.io.Source fromFile matFile getLines ) {
      val lineIter = line.split(" ").toIterator
      val y = FMat(lineIter.next.toFloat)
      var xr = List[Int]()
      var xc = List[Int]()
      var xv = List[Float]()
      while ( lineIter.hasNext ) { 
        xr = lineIter.next.toInt :: xr
        xc = 0 :: xc
        xv = lineIter.next.toFloat :: xv
      }
      val x = sparse(icol(xr), icol(xc), col(xv), NUM_FEATURES, 1)
      println(x)
      if ( ySoFar == null ) { 
        ySoFar = y
        xSoFar = x
      } else {
        ySoFar = ySoFar on y
        xSoFar = xSoFar \ x
      }
      if ( ySoFar.nrows == BLOCKSIZE ) {
        labels = ySoFar :: labels
        examples = xSoFar :: examples
        ySoFar = null
        xSoFar = null
      }
    }
    if ( ySoFar != null ) {
      labels = ySoFar :: labels
      examples = xSoFar :: examples
    }
    println("Loaded " + examples.length * BLOCKSIZE + " examples")
    return (examples, labels)
  }
}
