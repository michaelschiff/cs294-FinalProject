

import java.util.StringTokenizer
import java.io.BufferedReader
import java.io.FileReader
import java.io.File
import au.com.bytecode.opencsv.CSVReader
import scala.util.Random
import scala.collection.mutable.HashMap
import scala.collection.immutable.Set
import scala.math
import java.util.Arrays
import java.util.regex.Pattern
import java.util.regex.Matcher
import java.text.SimpleDateFormat
import java.util.Date

object PreProcessor {
   /* Top k most frequent terms
Presence of code sample
NLP related stuffs
User information
reputation?
Post Information
CreationDate Ã
ViewCount Ã
LastEditData Ã
LastActiityData Ã
AnswerCount Ã
CommentCount Ã
FavoriteCount Ã
*/
  
  /**
   *  0: Id
   *  1: Label
   *  2: Title
   *  3: Score
   *  4: Tags
   *  5: CreationDate
   *  6: ViewCount
   *  7: LastEditDate
   *  8: LastActivityDate
   *  9: Body
   * 10: AnswerCount
   * 11: CommentCount
   * 12: FavoriteCount
   * 13: NumEdits
   */
  
 
  def main(args: Array[String]): Unit = {
    val filename:String = "sample1.csv";
    
	parsePostData(filename)
  }
  
  def parsePostData(filename: String): List[List[String]] = {
    var data: List[List[String]] = List[List[String]]();
    
    var csvReader: CSVReader = new CSVReader(new FileReader(filename));
    
    // skip the first line of the file, which is the label line
    var currentLine: Array[String] = csvReader.readNext();
    currentLine = csvReader.readNext()

    while(currentLine != null) {
      if(currentLine.size == 14) {
        val id:String = currentLine(0)
    	val questionBody:String = currentLine(9)
    	val numCodeExamples: Integer = getNumInstances(Pattern.compile("<pre><code>"), questionBody)
    	val numEdits: Integer = currentLine(13).toInt
    	val score: Integer = currentLine(3).toInt
    	
    	val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss")
    	val creationDate: Date = dateFormat.parse(currentLine(5))
    	val lastActivityDate: Date = dateFormat.parse(currentLine(8))
    	
    	val editTimeElapsed: Long = if(currentLine(7) != "") dateFormat.parse(currentLine(7)).getTime() - creationDate.getTime() else 0
    	
    	data = List(id, numCodeExamples.toString(), numEdits.toString(), score.toString(), editTimeElapsed.toString()) :: data
      }
      currentLine = csvReader.readNext();
    }
    for(val line:List[String] <- data) {
      println(line.mkString("\t"))
    }
    data
    
  }
  
  def getNumInstances(pattern: Pattern, str: String): Integer = {
  var matches: Integer = 0
  val matcher: Matcher = pattern.matcher(str)
  while (matcher.find()) {
    matches += 1
  }
  return matches;
}
  
  
  
  

  


}
