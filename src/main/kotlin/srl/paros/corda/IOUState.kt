package srl.paros.corda

import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

// *********
// * State *
// *********
interface IOUState : ContractState {
  val value: Int
  val lender: Party
  val borrower: Party

  val data: String
  val contract get() = PlainIOUContract()
  override val participants get() = listOf<AbstractParty>(lender, borrower)
}

class PlainIOUState(
    override val value: Int,
    override val lender: Party,
    override val borrower: Party,
    override val data: String
) : IOUState