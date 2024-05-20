package decisions4s.dmn

import decisions4s.{DecisionTable, HKD}
import decisions4s.internal.HKDUtils
import org.camunda.bpm.model.dmn.instance.{Decision, Definitions, InputEntry, InputExpression, OutputEntry, Text, DecisionTable as DmnDecisionTable, Input as DmnInput, Output as DmnOutput, Rule as DmnRule}
import org.camunda.bpm.model.dmn.{Dmn, DmnModelInstance, HitPolicy}
import org.camunda.bpm.model.xml.instance.ModelElementInstance

import scala.reflect.ClassTag

object DmnConverter {

  def convert[Input[_[_]]: HKD, Output[_[_]]: HKD](table: DecisionTable[Input, Output]): DmnModelInstance = {
    val modelInstance = DmnBuilder(table).modelInstance
    Dmn.validateModel(modelInstance)
    modelInstance
  }

  private class DmnBuilder[Input[_[_]]: HKD, Output[_[_]]: HKD](table: DecisionTable[Input, Output]) {
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
      val table = decision.addChild[DmnDecisionTable]
      table.setHitPolicy(HitPolicy.FIRST)
      table
    }

    private def buildInputs(): Unit = {
      val names = HKDUtils.collectFields(table.inputNames)
      names.foreach(name => {
        val input = decisionTable.addChild[DmnInput]
        input.setLabel(name)
        input.addChild[InputExpression]
      })
    }

    private def buildOutputs(): Unit = {
      val names = HKDUtils.collectFields(table.outputNames)
      names.foreach(name => {
        val output = decisionTable.addChild[DmnOutput]
        output.setLabel(name)
      })
    }

    private def buildRules(): Unit = {
      table.rules.foreach(rule => {
        val ruleInstance      = decisionTable.addChild[DmnRule]
        val (inDesc, outDesc) = rule.render()
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
  }

}
