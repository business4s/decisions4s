package decisions4s.dmn

import cats.tagless.FunctorK
import decisions4s.DecisionTable
import decisions4s.internal.HKDUtils
import org.camunda.bpm.model.dmn.instance.{
  Decision,
  DecisionTable as DmnDecisionTable,
  Definitions,
  Input as DmnInput,
  InputEntry,
  InputExpression,
  Output as DmnOutput,
  OutputEntry,
  Rule as DmnRule,
  Text,
}
import org.camunda.bpm.model.dmn.{Dmn, DmnModelInstance}
import org.camunda.bpm.model.xml.instance.ModelElementInstance

import scala.reflect.ClassTag

object DmnConverter {

  def convert[Input[_[_]]: FunctorK, Output[_[_]]: FunctorK](table: DecisionTable[Input, Output]): DmnModelInstance = {
    val modelInstance = DmnBuilder(table).modelInstance
    Dmn.validateModel(modelInstance)
    modelInstance
  }

  private class DmnBuilder[Input[_[_]]: FunctorK, Output[_[_]]: FunctorK](table: DecisionTable[Input, Output]) {
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
      decision.setName(table.name.getOrElse("Decision"))
      decision
    }

    private def buildDecisionTable = decision.addChild[DmnDecisionTable]

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
