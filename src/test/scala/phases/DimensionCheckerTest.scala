package choicecalculus
package phases
package dimensionchecker

import org.scalatest._

import lang.trees.{ Dimension, Tree }
import lang.javascript.trees.BlockStmt
import lang.jscc.JsCcParser


import utility.test
import utility.messages._

class DimensionCheckerTest extends FlatSpec with matchers.ShouldMatchers {

  // equal is already defined in rewriter ...
  import org.scalatest.matchers.ShouldMatchers.{ equal => equal_ }  

  def dimensionCheckerError[T](block: => T): FatalPhaseError =
    evaluating {
      block
    } should produce [FatalPhaseError] match {
      case error => {
        error.phase should equal_('dimensionchecker)
        error
      }
    }

  trait Context extends Reader with Parser with Namer with DimensionChecker
      with JsCcParser with test.Helpers with namer.SymbolPreservingRewriter {

    def dimensionChecking(tree: Tree): Tree = {
      resetMessages()
      runReader(tree)
      runNamer(tree)
      runDimensionChecker(tree)
    }

  }

  it should "merge the choices of multiple subtrees correctly" in new Context {

    val ast: Dimension = dim('A)('a,'b) {
      BlockStmt(List(
        choice('A)('a -> lit("1"), 'b -> lit("2")),
        choice('A)('a -> lit("3"), 'b -> lit("4")),
        choice('A)('a -> lit("5"), 'b -> lit("6"))
      ))
    }

    val dimGraph = DependencyGraph(Set(DependentDimension(ast)))

    (dimensionChecking(ast)->dimensioning) should be (dimGraph)
  }

  // dim A(a, b) in {
  //   share #x: Expression as choice A {
  //       case a => 42
  //       case b => 48
  //     }
  //   within
  //     dim A(c, d) in #x
  // }
  it should "bind shared choices to the outer dimension (#7)" in new Context {
    val ticket7 = dim('A)('a,'b) {
      share('x, choice('A)(
        'a -> lit("42"),
        'b -> lit("48")
      ), choice('A)('a -> dim ('A)('c,'d) { id('x) }, 'b -> lit("47")))
    }

    dimensionChecking(ticket7)
  }

  it should "not allow declaring choices with missing alternatives" in new Context {

    val ast = dim('A)('a,'b) {
      choice('A)('a -> lit("42"))
    }

    dimensionCheckerError { dimensionChecking(ast) }
  }

  it should "select the first of two nested, equally named dimensions (#8)" in new Context {
    def ast = dim('A)('a,'b) { dim('A)('b,'c) { lit("42") } }

    dimensionChecking(select('A, 'a, ast))
    dimensionChecking(select('A, 'b, ast))
    dimensionCheckerError { dimensionChecking(select('A, 'c, ast)) }
  }

  "Open questions from vamos 2013" should
    "4.1 warn when selecting undeclared dimensions" in new Context {

    def example4_1 = select('D, 't, lit("1") + lit("2"))
    dimensionChecking(example4_1)
    vacuousWarning should be (true)
  }

  it should "4.2 not allow multiple, parallel dimension declarations" in new Context {
    def example4_2 = select('D, 'a,
      dim('D)('a, 'b) { choice('D) (
        'a -> lit("1"),
        'b -> lit("2")
      )} + dim('D)('a, 'c) { choice('D) (
        'a -> lit("3"),
        'c -> lit("4")
      )})

    dimensionCheckerError { dimensionChecking(example4_2) }
  }

  it should "4.2 allow atomic choices as exception to parallel dimension declaration" in new Context {
    def example4_2_1 = share('x, 
      dim('A)('a, 'b) { choice('A) ('a -> lit("1"), 'b -> lit("1")) },
      id('x) + id('x))
      dimensionChecking(example4_2_1)
  }

  it should "4.3 not allow undeclared tag selection" in new Context {
    def example4_3 = select('D,'a, dim('D)('b, 'c) { choice('D) (
      'b -> lit("1"),
      'c -> lit("2")
    )})
    def example4_3_2 = select('D,'a, dim('D)('b, 'd) { lit("3") + lit("4") })

    dimensionCheckerError { dimensionChecking(example4_3) }
    dimensionCheckerError { dimensionChecking(example4_3_2) }
  }

  it should "4.4 warn when selecting into dependent dimensions" in new Context {

    //select B.c from
    //  dim A(a,b) in
    //   choice A {
    //     case a => dim B(c,d) in choice B {
    //       case c => 1
    //       case d => 2
    //     }
    //     case b => 3
    //   }
    def example4_4 = select('B, 'c, dim('A)('a, 'b) {
      choice('A) (
        'a -> dim('B)('c, 'd) { choice('B) (
          'c -> lit("1"),
          'd -> lit("2")
        )},
        'b -> lit("3")
      )})

    dimensionChecking(example4_4)
    dependentWarning should be (true)
  }
}