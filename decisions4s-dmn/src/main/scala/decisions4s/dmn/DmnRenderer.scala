package decisions4s.dmn

import decisions4s.DecisionTable
import decisions4s.internal.RenderUtils
import decisions4s.internal.RenderUtils.DecisionRenderInput
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

object DmnRenderer {

  def render[In[_[_]], Out[_[_]]](table: DecisionTable[In, Out, ?]): DMNModel = {
    val modelInstance = DmnBuilder(RenderUtils.prepare(table)).modelInstance
    Dmn.validateModel(modelInstance)
    DMNModel(modelInstance)
  }

  private class DmnBuilder[In[_[_]], Out[_[_]]](table: DecisionRenderInput) {

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
      decision.setId("decision_1")
      decision
    }

    private def buildDecisionTable = {
      val tableElem = decision.addChild[DmnDecisionTable]
      tableElem.setId("table_1")
      tableElem.setHitPolicy((table.hitPolicy: decisions4s.HitPolicy) match {
        case decisions4s.HitPolicy.Single       => HitPolicy.UNIQUE
        case decisions4s.HitPolicy.Distinct     => HitPolicy.ANY
        case decisions4s.HitPolicy.First        => HitPolicy.FIRST
        case decisions4s.HitPolicy.Collect      => HitPolicy.COLLECT
        case decisions4s.HitPolicy.CollectSum   => HitPolicy.COLLECT
        case decisions4s.HitPolicy.CollectMin   => HitPolicy.COLLECT
        case decisions4s.HitPolicy.CollectMax   => HitPolicy.COLLECT
        case decisions4s.HitPolicy.CollectCount => HitPolicy.COLLECT
      })
      val aggr      = (table.hitPolicy: decisions4s.HitPolicy) match {
        case decisions4s.HitPolicy.CollectSum   => Some(BuiltinAggregator.SUM)
        case decisions4s.HitPolicy.CollectMin   => Some(BuiltinAggregator.MIN)
        case decisions4s.HitPolicy.CollectMax   => Some(BuiltinAggregator.MAX)
        case decisions4s.HitPolicy.CollectCount => Some(BuiltinAggregator.COUNT)
        case _                                  => None
      }
      aggr.foreach(tableElem.setAggregation)
      tableElem
    }

    private def buildInputs(): Unit = {
      table.inputNames.zipWithIndex.foreach((name, idx) => {
        val input = decisionTable.addChild[DmnInput]
        input.setLabel(name)
        input.setId(s"input_${idx}")
        val expr  = input.addChild[InputExpression]
        expr.setId(s"input_${idx}_expr_1")
      })
    }

    private def buildOutputs(): Unit = {
      table.outputNames.zipWithIndex.foreach((name, idx) => {
        val output = decisionTable.addChild[DmnOutput]
        output.setId(s"output_${idx}")
        output.setLabel(name)
      })
    }

    private def buildRules(): Unit = {
      table.rules.zipWithIndex.foreach((rule, ruleIdx) => {
        val ruleInstance = decisionTable.addChild[DmnRule]
        ruleInstance.setId(s"rule_${ruleIdx}")
        rule.annotation.foreach(ruleInstance.setDescription(_))
        rule.inputs.zipWithIndex
          .foreach((input, idx) => {
            val entry = ruleInstance.addChild[InputEntry]
            entry.setId(s"rule_${ruleIdx}_input_${idx}")
            entry.setText(input)
          })
        rule.outputs.zipWithIndex
          .foreach((output, idx) => {
            val entry = ruleInstance.addChild[OutputEntry]
            entry.setId(s"rule_${ruleIdx}_output_${idx}")
            entry.setText(output)
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
