package srl.paros.corda

import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.crypto.SecureHash
import net.corda.core.transactions.LedgerTransaction

interface IOUContract : Contract {
  override fun verify(tx: LedgerTransaction) {
    val command = tx.commands.requireSingleCommand<IOUCreate>()

    requireThat {
      // Constraints on the shape of the transaction.
      "No inputs should be consumed when issuing an IOU." using tx.inputs.isEmpty()
      "There should be one output state." using (tx.outputs.size == 1)

      // IOU-specific constraints.
      "The output should be IOUState type." using (tx.outputs.single().data is IOUState)
      val out = tx.outputs.single().data as IOUState
      "The IOU's value must be non-negative." using (out.value > 0)
      "The lender and the borrower cannot be the same entity." using (out.lender != out.borrower)

      // Constraints on the signers.
      "There must only be one signer." using (command.signers.toSet().size == 1)
      "The signer must be the lender." using (command.signers.contains(out.lender.owningKey))
    }
  }

}

class PlainIOUContract : IOUContract