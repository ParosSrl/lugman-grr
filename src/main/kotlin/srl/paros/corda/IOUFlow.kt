package srl.paros.corda

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.security.PublicKey
import kotlin.reflect.jvm.jvmName

// *********
// * Flows *
// *********

@InitiatingFlow
@StartableByRPC
class IOUStartFlow(val iou: Int, val party: Party) : FlowLogic<Unit>() {
  override val progressTracker = ProgressTracker()

  @Suspendable
  override fun call() {
    val me = serviceHub.myInfo.legalIdentities[0]
    val notary = serviceHub.networkMapCache.notaryIdentities[0]

    val state = PlainIOUState(iou, me, party, "")
    val contract = IOUContract::class.jvmName
    val snc = StateAndContract(state, contract)

    val command = Command(PlainIOUCreate(), me.owningKey)

    val tx = TransactionBuilder(notary = notary)
        .withItems(snc, command)

    tx.verify(serviceHub)

    val signedTx = serviceHub.signInitialTransaction(tx)

    subFlow(FinalityFlow(signedTx))
  }
}

@InitiatedBy(IOUStartFlow::class)
class IOUBusinessFlow(val otherParty: Party) : FlowLogic<Unit>() {
  @Suspendable
  override fun call() {
    return Unit
  }
}