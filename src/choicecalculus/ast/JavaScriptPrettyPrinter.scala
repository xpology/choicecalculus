package choicecalculus.ast

import org.kiama.output.ParenPrettyPrinter

trait JavaScriptPP extends ParenPrettyPrinter with org.kiama.output.PrettyPrinter  {
  
  def toDocOrEmpty(e: Option[ASTNode]) = e match {
    case Some(e) => toDoc(e)
    case None => empty
  }
  
  def toDoc(e: ASTNode): Doc = e match {

    case Literal(contents) => 
      text(contents) 
    
    case Program(contents) => 
      lsep (contents.map(toDoc), semi)

    case VarDeclStmt(bindings) => 
      "var" <+> ssep (bindings.map(toDoc), comma)
    
    case VarBinding(name, binding) => 
      toDoc(name) <+> equal <+> toDoc(binding)
      
    case BlockStmt(stmts) => 
      braces( nest( line <> lsep(stmts.map( toDoc(_) ), semi) <> line ))
    
    case IfStmt(cond, thenBlock, None) => 
      "if" <> parens(toDoc(cond)) <+> toDoc(thenBlock) 
      
    case IfStmt(cond, thenBlock, Some(elseBlock)) => 
      "if" <> parens(toDoc(cond)) <+> toDoc(thenBlock) <+> "else" <+> toDoc(elseBlock)
      
    case WhileStmt(cond, body) => 
      "while" <> parens(toDoc(cond)) <+> toDoc(body)
    
    case DoWhileStmt(body, cond) => 
      "do" <+> toDoc(body) <+> "while" <> parens(toDoc(cond))
    
    case ForStmt(init, cond, incr, body) => 
      "for" <> parens(toDoc(init) <+> semi <+> toDocOrEmpty(cond) <+> semi <+> toDocOrEmpty(incr)) <+> toDoc(body)
      
    case ForInStmt(init, collection, body) => 
      "for" <> parens(toDoc(init) <+> "in" <+> toDoc(collection)) <+> toDoc(body)
      
    case SwitchStmt(head, cases) => 
      "switch" <> parens(toDoc(head)) <+> braces(nest (line <> lsep (cases.map(toDoc), line) <> line))

    case MatchingCase(matcher, stmts) => 
      "case" <+> toDoc(matcher) <> ":" <> nest(line <> lsep (stmts.map(toDoc), line))
      
    case DefaultCase(stmts) => 
      "default" <> ":" <> nest(line <> lsep (stmts.map(toDoc), line))
      
    case BreakStmt(label) => 
      "break" <+> toDocOrEmpty(label)
      
    case ContinueStmt(label) => 
      "continue" <+> toDocOrEmpty(label)
      
    case ThrowStmt(body) => 
      "throw" <+> toDoc(body)

    case TryStmt(body, Some(catchBlock), Some(finallyBlock)) => 
      "try" <+> toDoc(body) <+> toDoc(catchBlock) <+> toDoc(finallyBlock)
      
    case CatchBlock(name, body) => 
      "catch" <> parens( toDoc(name) ) <+> toDoc(body)
      
    case FinallyBlock(body) => 
      "finally" <+> toDoc(body)

    case ReturnStmt(body) => 
      "return" <+> toDoc(body)
    
    case WithStmt(binding, body) => 
      "with" <> parens(toDoc(binding)) <+> toDoc(body)
      
    case LabeledStmt(label, body) => 
      toDoc(label) <> ":" <+> toDoc(body)
      
    case EmptyStmt => semi

    case BinaryOpExpr(lhs, op, rhs) => 
      toDoc(lhs) <+> text(op) <+> toDoc(rhs)
    
    case TernaryExpr(cond, trueBlock, falseBlock) => 
      toDoc(cond) <+> question <+> toDoc(trueBlock) <+> colon <+> toDoc(falseBlock)
      
    case PrefixExpr(op, body) => 
      text(op) <+> toDoc(body)
      
    case PostfixExpr(body, op) => 
      toDoc(body) <> text(op)
      
    case NewExpr(body) =>
      "new" <+> toDoc(body)
      
    case CallExpr(body, args) =>
      toDoc(body) <> parens( ssep( args.map(toDoc), comma) )
      
    case MemberExpr(body, access) => 
      toDoc(body) <> "[" <> toDoc(access) <> "]"
      
    case NameAccessExpr(body, name) =>
      toDoc(body) <> dot <> toDoc(name)
      
    case ArrayExpr(contents) =>
      "[" <> ssep(contents.map(toDoc), comma) <> "]"
      
      
    case GroupExpr(content) => 
      parens( toDoc(content) )
      
    // TODO use softbreaks here
    case ObjectExpr(bindings) => 
      braces( nest( softline <> lsep(bindings.map(toDoc), comma) <> softline ) )
      
    case PropertyBinding(name, value) =>
      toDoc(name) <> colon <+> toDoc(value)
      
    case FunctionDecl(name, args, body) =>
      "function" <+> toDoc(name) <> parens( ssep(args.map(toDoc), comma) ) <+> toDoc(body)
      
    case FunctionExpr(args, body) => 
      "function" <> parens( ssep(args.map(toDoc), comma) ) <+> toDoc(body)
  }
  
}