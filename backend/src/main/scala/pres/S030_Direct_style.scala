package pres

class S030_Direct_style:

  val What_is_it = """
    Direct style is an approach to programming where the results of effectful
    computations are available directly, without a "wrapper" type such as
    Future, IO or Task.
  """

  val What_is_it_v2 = """
    Direct style is the opposite of continuation-passing style and a control monad.
  """

  val Benefits = List(
    "Direct syntax: no overhead",
    "Meaningful stack traces",
    "Built-in control-flow constructs"
  )
