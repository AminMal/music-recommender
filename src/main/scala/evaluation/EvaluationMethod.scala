package ir.ac.usc
package evaluation

import org.apache.spark.sql.DataFrame

trait EvaluationMethod {

  def trainData: DataFrame
  def testData: DataFrame
  def data: (DataFrame, DataFrame)

}
