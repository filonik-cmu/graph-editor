package editor.pages

import scala.scalajs.js
import scala.scalajs.js.JSON

import com.raquo.laminar.api.L.{*, given}

import editor.models.{*,given}
import editor.models.companies.*
import editor.utilities.*

object SignalObserverPage:
  private val companyIdGen = new IntGenerator()
  private val departmentIdGen = new IntGenerator()

  private val databaseVar = Var(Database(
    companies = Seq(
      Company(companyIdGen().toString(), "HelloWorld Inc"),
      Company(companyIdGen().toString(), "Wayland Corp"),
    ),
  ))

  private val databaseSignal: Signal[Database] = databaseVar.signal
  private val databaseUpdate: Observer[Update[Database]] = Observer[Update[Database]] {
    databaseVar.update(_)
  }

  private val (companiesSignal, companiesUpdate) = Update.zoom(databaseSignal, databaseUpdate)(
    _.companies
  )(
    (database, companies) => database.copy(companies=companies)
  )

  private val (departmentsSignal, departmentsUpdate) = Update.zoom(databaseSignal, databaseUpdate)(
    _.departments
  )(
    (database, departments) => database.copy(departments=departments)
  )
  
  def replaceValue[A]: A => A => A = value => _ => value

  object TextInput:
    type Model = String
    def apply(modelSignal: Signal[Model], modelUpdate: Observer[Update[Model]]): HtmlElement = 
      input(
        `type` := "text",
        controlled(
          value <-- modelSignal,
          onInput.mapToValue.map(replaceValue) --> modelUpdate
        )
      )
  
  object DepartmentItem:
    type Model = Department
    def apply(modelSignal: Signal[Model], modelUpdater: Observer[Update[Model]])(mods: Modifier[HtmlElement]*): HtmlElement = 
      val (nameSignal, nameUpdater) = Update.zoom(modelSignal, modelUpdater)(
        _.name
      )(
        (company, name) => company.copy(name=name)
      )
      li(
        div(
          TextInput(nameSignal, nameUpdater),
          mods
        ),
      )
  
  object DepartmentList:
    type Model = Seq[Department]
    def apply(modelSignal: Signal[Model], modelUpdate: Observer[Update[Model]]): HtmlElement = 
      ul(
        children <-- modelSignal.split(_.id){(id, _, departmentSignal) =>
          val deleteDepartment: Update[Model] = _.delete(_.id == id, (_) => {})
          DepartmentItem(departmentSignal, modelUpdate.contramap(
            (departmentUpdate) => _.update(_.id == id, departmentUpdate)
          ))(
            button(
              cls := "btn-sm btn-red",
              onClick.mapTo(deleteDepartment) --> modelUpdate,
              "Delete"
            )
          )
        }
      )
  
  object CompanyItem:
    type Model = Company
    def apply(modelSignal: Signal[Model], modelUpdater: Observer[Update[Model]])(mods: Modifier[HtmlElement]*):  HtmlElement = 
      val companyDepartmentsSignal = modelSignal.combineWith(departmentsSignal).map(
        (company, departments) => departments.filter(company.id == _.companyId)
      )
      val (nameSignal, nameUpdater) = Update.zoom(modelSignal, modelUpdater)(
        _.name
      )(
        (company, name) => company.copy(name=name)
      )
      li(
        div(
          TextInput(nameSignal, nameUpdater),
          mods
        ),
        DepartmentList(companyDepartmentsSignal, departmentsUpdate),
      )
  
  object CompanyList:
    type Model = Seq[Company]
    def apply(modelSignal: Signal[Model], modelUpdate: Observer[Update[Model]]): HtmlElement = 
      ul(
        children <-- modelSignal.split(_.id){(id, _, companySignal) =>
          val createDepartment: Update[Seq[Department]] = _.create(
            () => Department(departmentIdGen().toString(), id, "Unnamed")
          )
          val deleteCompany: Update[Model] = _.delete(_.id == id, (_) => {})
          CompanyItem(companySignal, modelUpdate.contramap(
            (departmentUpdate) => _.update(_.id == id, departmentUpdate)
          ))(
            button(
              cls := "btn-sm btn-green",
              onClick.mapTo(createDepartment) --> departmentsUpdate,
              "Create"
            ),
            button(
              cls := "btn-sm btn-red",
              onClick.mapTo(deleteCompany) --> modelUpdate,
              "Delete"
            )
          )
        }
      )
  
  object DatabaseItem:
    def apply(mods: Modifier[HtmlElement]*): HtmlElement = 
      div(
        div(
          mods
        ),
        CompanyList(companiesSignal, companiesUpdate),
      )

  def apply: HtmlElement = 
    val createCompany: Update[Seq[Company]] = _.create(
      () => Company(departmentIdGen().toString(), "Unnamed")
    )
    div(
      cls := "grid grid-cols-2 overflow-hidden",
      DatabaseItem(
        button(
          cls := "btn-sm btn-green",
          onClick.mapTo(createCompany) --> companiesUpdate,
          "Create"
        ),
      ),
      pre(
        cls := "text-xs overflow-auto",
        child.text <-- databaseSignal.map(
          (database) => JSON.stringify(database.asInstanceOf[js.Any], space=2)
        )
      ),
    )
