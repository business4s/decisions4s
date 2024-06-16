package decisions4s.dmn

import decisions4s.DecisionTable
import decisions4s.internal.HKDUtils
import org.camunda.bpm.model.dmn.instance.{
  Decision,
  DecisionTable as DmnDecisionTable,
  Definitions,
  Description,
  Input as DmnInput,
  InputEntry,
  InputExpression,
  Output as DmnOutput,
  OutputEntry,
  Rule as DmnRule,
  Text,
}
import org.camunda.bpm.model.dmn.{BuiltinAggregator, Dmn, DmnModelInstance, HitPolicy}
import org.camunda.bpm.model.xml.instance.ModelElementInstance

import scala.reflect.ClassTag

object DmnConverter {

  def convert[Input[_[_]], Output[_[_]], HP <: DecisionTable.HitPolicy](table: DecisionTable[Input, Output, HP]): DmnModelInstance = {
    val modelInstance = DmnBuilder(table).modelInstance
    Dmn.validateModel(modelInstance)
    modelInstance
  }

  private class DmnBuilder[Input[_[_]], Output[_[_]], HP <: DecisionTable.HitPolicy](table: DecisionTable[Input, Output, HP]) {
    import table.given

    val modelInstance: DmnModelInstance  = Dmn.createEmptyModel()
    private val definitions: Definitions = buildDefinitions
    private val decision                 = buildDecision
    private val decisionTable            = buildDecisionTable
    buildInputs()
    buildOutputs()
    buildRules()

    private def buildDefinitions: Definitions = {
      val definitions = modelInstance.newInstance(classOf[Definitions])
      definitions.setNamespace("http://camunda.org/schema/1.0/dmn")
      definitions.setName("definitions")
      definitions.setId("definitions")
      modelInstance.setDefinitions(definitions)
      definitions
    }

    private def buildDecision = {
      val decision = definitions.addChild[Decision]
      decision.setName(table.name)
      decision
    }

    private def buildDecisionTable = {
      val tableElem = decision.addChild[DmnDecisionTable]
      tableElem.setHitPolicy((table.hitPolicy: DecisionTable.HitPolicy) match {
        case DecisionTable.HitPolicy.Single       => HitPolicy.UNIQUE
        case DecisionTable.HitPolicy.Distinct     => HitPolicy.ANY
        case DecisionTable.HitPolicy.First        => HitPolicy.FIRST
        case DecisionTable.HitPolicy.Collect      => HitPolicy.COLLECT
        case DecisionTable.HitPolicy.CollectSum   => HitPolicy.COLLECT
        case DecisionTable.HitPolicy.CollectMin   => HitPolicy.COLLECT
        case DecisionTable.HitPolicy.CollectMax   => HitPolicy.COLLECT
        case DecisionTable.HitPolicy.CollectCount => HitPolicy.COLLECT
      })
      val aggr      = (table.hitPolicy: DecisionTable.HitPolicy) match {
        case DecisionTable.HitPolicy.CollectSum   => Some(BuiltinAggregator.SUM)
        case DecisionTable.HitPolicy.CollectMin   => Some(BuiltinAggregator.MIN)
        case DecisionTable.HitPolicy.CollectMax   => Some(BuiltinAggregator.MAX)
        case DecisionTable.HitPolicy.CollectCount => Some(BuiltinAggregator.COUNT)
        case _                                    => None
      }
      aggr.foreach(tableElem.setAggregation)
      tableElem
    }

    private def buildInputs(): Unit = {
      val names = table.inputHKD.fieldNames
      names.foreach(name => {
        val input = decisionTable.addChild[DmnInput]
        input.setLabel(name)
        input.addChild[InputExpression]
      })
    }

    private def buildOutputs(): Unit = {
      val names = table.outputHKD.fieldNames
      names.foreach(name => {
        val output = decisionTable.addChild[DmnOutput]
        output.setLabel(name)
      })
    }

    private def buildRules(): Unit = {
      table.rules.foreach(rule => {
        val ruleInstance      = decisionTable.addChild[DmnRule]
        val (inDesc, outDesc) = rule.render()
        rule.annotation.foreach(ruleInstance.setDescription(_))
        HKDUtils
          .collectFields(inDesc)
          .foreach(desc => {
            val entry = ruleInstance.addChild[InputEntry]
            entry.setText(desc)
          })
        HKDUtils
          .collectFields(outDesc)
          .foreach(desc => {
            val entry = ruleInstance.addChild[OutputEntry]
            entry.setText(desc)
          })
      })
    }

  }

  extension (elem: ModelElementInstance) {
    def addChild[T <: ModelElementInstance](using ct: ClassTag[T]): T = {
      val instance: T = elem.getModelInstance.newInstance(ct.runtimeClass.asInstanceOf[Class[T]])
      elem.addChildElement(instance)
      instance
    }
    def setText(value: String): Unit                                  = {
      val text = addChild[Text]
      text.setTextContent(value)
    }
    def setDescription(value: String): Unit                           = {
      val text = addChild[Description]
      text.setTextContent(value)
    }
  }

}
