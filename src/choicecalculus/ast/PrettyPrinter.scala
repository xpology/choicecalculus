package choicecalculus
package ast

import org.kiama.output.ParenPrettyPrinter

trait PrettyPrinter extends ParenPrettyPrinter with org.kiama.output.PrettyPrinter  {
  
  def toDoc(e: ASTNode): Doc
  
}

trait ChoiceCalcPP extends PrettyPrinter {

  override def toDoc(e: ASTNode): Doc = e match {
    
    case DimensionExpr(dim, tags, body) => 
      "dim" <+> text(dim.name) <> "<" <> fillsep(tags.map( (t) => text(t.name)), comma) <> ">" <+> 
        "in" <+> toDoc(body)
    
    case ChoiceExpr(dim, choices) =>
      "choice" <+> text(dim.name) <+> 
        braces (nest (line <> ssep (choices.map(toDoc), line)) <> line)
    
    case Choice(tag, body) =>
      "case" <+> text(tag.name) <+> "=>" <+> toDoc(body)
      
    case SelectExpr(dim, tag, body) =>
      "select" <+> text(dim.name) <> dot <> text(tag.name) <+> "from" <+> toDoc(body)
      
    case IdExpr(id) =>
      text(id.name)
      
    case ShareExpr(x, binding, body) =>
      "share" <+> text(x.name) <+> equal <+> toDoc(binding) <+> "in" <+> toDoc(body)
    
    case PartialConfig(body, configs) =>      
      configs.foldLeft(toDoc(body)) {
        (old, config) => "select" <+> text(config._1.name) <> dot <> text(config._2.name) <+> "from" <+> old 
      }
    
    case other => text(other.toString)       
  } 
}

/**
 * Some Hostlanguage Prettyprinter ready to be mixed in with the Choice Calculus PP
 */
trait HostlanguagePP extends ChoiceCalcPP {
  
  override def toDoc(e: ASTNode): Doc = e match {
    
    case GroupExpr(body) => parens(toDoc(body))
    
    case FunctionExpr(body) => "function" <> "(" <> ")" <+> braces(nest( line <> toDoc(body)) <> line)
    
    case Add(lhs, rhs) => toDoc(lhs) <+> plus <+> toDoc(rhs)
    
    case Mul(lhs, rhs) => toDoc(lhs) <+> asterisk <+> toDoc(rhs)
    
    case Num(n) => text(n.toString)
    
    case other => super.toDoc(other)
  }
  
}