package choicecalculus
package namer

import lang.ASTNode
import lang.choicecalculus.{ Identifier, Share }

import utility.Messaging.error
import utility.AttributableRewriter.Term
import utility.Attribution.{ attr, paramAttr }
import utility.Attribution.initTree
import utility.DebugRewriter.{ everywheretd, query }

/**
 * The `Namer` phase
 */
trait Namer {

  /**
   * Decorates the given tree with references from variables to their
   * binding instances.
   * 
   * It should be used before any rewriting takes places since rewriting 
   * might destroy the parent chain which is essential to lookup identifiers.
   *
   * After triggering the resolution, in all follow up phases the binding instance
   * of an `Identifier` can be resolved by `id->bindingInstance`.
   *
   * @see [[bindingInstance]]
   */
  def runNamer(ast: ASTNode): ast.type = { 
    initTree(ast)
    everywheretd (forceNameResolution) (ast);
    ast 
  }

  /**
   * Resolves the binding of the given `Identifier`
   *
   * @example {{{ 
   *   val id = Identifier('x)
   *   val tree = Share('x, ..., Add(id, Numeral(4)))
   *   initTree(tree)
   *   id->bindingInstance // => tree
   * }}}
   *
   * @param identifier to resolve the binding from
   * @return the some share expression that binds the `Identifier` it is called on
   *         or `None` if the `Identifier` is not bound.
   */
  val bindingInstance: Identifier[ASTNode] => Option[Share[_,_]] = attr { 
    case p => p->bindingInstanceOf(p)
  }

  private val forceNameResolution = query { 
    case id: Identifier[ASTNode] => id->bindingInstance match {
      case None => error(id, s"Use of unbound choice calculus variable '${id.name.name}'")
      case _ =>
    }
  }

  private val bindingInstanceOf: Identifier[ASTNode] => ASTNode => Option[Share[_,_]] = paramAttr {
    case id@Identifier(name) => {
      case s@Share(`name`, _, _) => Some(s)
      case node if node.isRoot => None
      
      // If the parent is a share expression we have to check whether we are in the binding branch
      // of the share. Otherwise this would create circular bindings, much like a letrec.
      case node => node.parent match {
        case p@Share(_, `node`, _) => p.parent[ASTNode]->bindingInstanceOf(id)
        case otherParent: ASTNode => otherParent->bindingInstanceOf(id)
        case _ => None
      }
    }
  }

}