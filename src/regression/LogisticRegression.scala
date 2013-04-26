package LogisticRegression

import BIDMat.{Mat, FMat, DMat, IMat, CMat, BMat, CSMat, SMat, SDMat, GMat, GIMat, GSMat, HMat}
import BIDMat.MatFunctions._
import BIDMat.SciFunctions._
import BIDMat.Solvers._
import BIDMat.Plotting._

object classify extends LoadsMat {
  Mat.noMKL=true  //can I comment this out for better performance???
  NUM_FEATURES = 3
  def main(args:Array[String]):Unit = {
    val (examples, labels) = loadMat(args(0))
    val classifier = new simpleClassifier(examples, labels, NUM_FEATURES, 10000)
    classifier.train
  }
}

class simpleClassifier (examples:List[SMat], labels:List[FMat], num_features:Int, max_iters:Int) extends Classifies { 
  var weights = ones(1, num_features)
  def train(): Unit = {
    var iter = 0
    while ( iter < max_iters ) {
      for ( (x,y) <- examples zip labels ) {
        weights = weights + update(x, y, weights)
      }
    }
  }
}



trait Classifies {
  var ALPHA = 0.00000001f
  def update(examples: SMat, labels: FMat, weights: FMat): FMat = {
    val predictions:FMat = (weights * examples).t
    val z:FMat = -1 * predictions
    val logit:FMat = 1.0 /@ (1 + exp(z))
    val differences = labels - logit
    println(scala.math.sqrt(sum((differences *@ differences), 1)(0,0))/differences.nrows)
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
