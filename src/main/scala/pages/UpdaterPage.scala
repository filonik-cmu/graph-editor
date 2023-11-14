package editor.pages

import scala.scalajs.js
import scala.scalajs.js.JSON

import com.raquo.laminar.api.L.{*, given}

import editor.models.{Resource, Updater}
import editor.models.companies.*
import editor.utilities.*

object UpdaterPage:
  private val companyIdGen = new IntGenerator()
  private val departmentIdGen = new IntGenerator()

  private val databaseVar = Var(Database(
    companies = Seq(
      Company(companyIdGen().toString(), "HelloWorld Inc"),
      Company(companyIdGen().toString(), "Wayland Corp"),
    ),
  ))

  private val databaseSignal: Signal[Database] = databaseVar.signal
  private val databaseUpdater: Updater[Database] = databaseVar.update(_)

  private val (companiesSignal, companiesUpdater) = Updater.zoom(databaseSignal, databaseUpdater)(
    _.companies
  )(
    (database, companies) => database.copy(companies=companies)
  )

  private val (departmentsSignal, departmentsUpdater) = Updater.zoom(databaseSignal, databaseUpdater)(
    _.departments
  )(
    (database, departments) => database.copy(departments=departments)
  )
  
  object TextInput:
    type Model = String
    def apply(modelSignal: Signal[Model], modelUpdater: Updater[Model]): HtmlElement = 
      def modelWriter = { (value: Model) => modelUpdater((_) => value) }
      input(
        `type` := "text",
        controlled(
          value <-- modelSignal,
          onInput.mapToValue --> modelWriter
        )
      )
  
  object DepartmentItem:
    type Model = Department
    def apply(modelSignal: Signal[Model], modelUpdater: Updater[Model])(mods: Modifier[HtmlElement]*): HtmlElement = 
      val (nameSignal, nameUpdater) = Updater.zoom(modelSignal, modelUpdater)(
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
    def apply(modelSignal: Signal[Model], modelUpdater: Updater[Model]): HtmlElement = 
      def departmentCreator = Resource.creator(modelUpdater)
      ul(
        children <-- modelSignal.split(_.id){(id, _, departmentSignal) =>
          def departmentUpdater = Resource.updater(modelUpdater, _.id == id)
          def departmentDeleter = Resource.deleter(modelUpdater, _.id == id)
          DepartmentItem(departmentSignal, departmentUpdater)(
            button(
              cls := "btn-sm btn-red",
              onClick --> (_ => departmentDeleter(
                (_) => {}
              )),
              "Delete"
            )
          )
        }
      )
  
  object CompanyItem:
    type Model = Company
    def apply(modelSignal: Signal[Model], modelUpdater: Updater[Model])(mods: Modifier[HtmlElement]*):  HtmlElement = 
      val companyDepartmentsSignal = modelSignal.combineWith(departmentsSignal).map(
        (company, departments) => departments.filter(company.id == _.companyId)
      )
      val (nameSignal, nameUpdater) = Updater.zoom(modelSignal, modelUpdater)(
        _.name
      )(
        (company, name) => company.copy(name=name)
      )
      li(
        div(
          TextInput(nameSignal, nameUpdater),
          mods
        ),
        DepartmentList(companyDepartmentsSignal, departmentsUpdater),
      )
  
  object CompanyList:
    type Model = Seq[Company]
    def apply(modelSignal: Signal[Model], modelUpdater: Updater[Model]): HtmlElement = 
      ul(
        children <-- modelSignal.split(_.id){(id, _, companySignal) =>
          def departmentCreator = Resource.creator(departmentsUpdater)
          def companyUpdater = Resource.updater(companiesUpdater, _.id == id)
          def companyDeleter = Resource.deleter(companiesUpdater, _.id == id)
          CompanyItem(companySignal, companyUpdater)(
            button(
              cls := "btn-sm btn-green",
              onClick --> (_ => departmentCreator(
                () => Department(companyIdGen().toString(), id, "Unnamed")
              )),
              "Create"
            ),
            button(
              cls := "btn-sm btn-red",
              onClick --> (_ => companyDeleter(
                (_) => {}
              )),
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
        CompanyList(companiesSignal, companiesUpdater),
      )

  def apply: HtmlElement = 
    def companyCreator = Resource.creator(companiesUpdater)
    //databaseVar.zoom((db) => db.companies)((db, companies) => db.copy(companies=companies))
    div(
      cls := "grid grid-cols-2 overflow-hidden",
      DatabaseItem(
        button(
          cls := "btn-sm btn-green",
          onClick --> (_ => companyCreator(
            () => Company(companyIdGen().toString(), "Unnamed")
          )),
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
