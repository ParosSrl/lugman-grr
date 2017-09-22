package srl.paros.corda

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.CordaPluginRegistry
import net.corda.core.serialization.SerializationCustomization
import net.corda.core.transactions.LedgerTransaction
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

// *****************
// * API Endpoints *
// *****************
@Path("template")
class TemplateApi(val services: CordaRPCOps) {
    // Accessible at /api/template/templateGetEndpoint.
    @GET
    @Path("templateGetEndpoint")
    @Produces(MediaType.APPLICATION_JSON)
    fun templateGetEndpoint(): Response {
        return Response.ok(mapOf("message" to "Template GET endpoint.")).build()
    }
}

// *****************
// * Contract Code *
// *****************
// This is used to identify our contract when building a transaction
val TEMPLATE_CONTRACT_ID = "com.template.TemplateContract"

class Create : CommandData

class IOUContract : Contract {
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Create>()

        requireThat {
            // Constraints on the shape of the transaction.
            "No inputs should be consumed when issuing an IOU." using tx.inputs.isEmpty()
            "There should be one output state." using(tx.outputs.size == 1)

            // IOU-specific constraints.
            "The output should be IOUState type." using(tx.outputs.single().data is IOUState)
            val out = tx.outputs.single().data as IOUState
            "The IOU's value must be non-negative." using (out.value > 0)
            "The lender and the borrower cannot be the same entity." using (out.lender != out.borrower)

            // Constraints on the signers.
            "There must only be one signer." using (command.signers.toSet().size == 1)
            "The signer must be the lender." using (command.signers.contains(out.lender.owningKey))
        }
    }

    // The legal contract reference - we'll leave this as a dummy hash for now.
    override val legalContractReference = SecureHash.zeroHash
}

// *********
// * State *
// *********
interface IOUState : ContractState {
    val value: Int
    val lender: Party
    val borrower: Party

    val data: String
    override val contract get() = IOUContract()
    override val participants get() = listOf<AbstractParty>(lender, borrower)
}

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class Initiator : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        return Unit
    }
}

@InitiatedBy(Initiator::class)
class Responder(val otherParty: Party) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        return Unit
    }
}

// *******************
// * Plugin Registry *
// *******************
class TemplatePlugin : CordaPluginRegistry() {
    // Whitelisting the required types for serialisation by the Corda node.
    override fun customizeSerialization(custom: SerializationCustomization): Boolean {
        return true
    }
}

class TemplateWebPlugin : WebServerPluginRegistry {
    // A list of classes that expose web JAX-RS REST APIs.
    override val webApis: List<Function<CordaRPCOps, out Any>> = listOf(Function(::TemplateApi))
    //A list of directories in the resources directory that will be served by Jetty under /web.
    // This template's web frontend is accessible at /web/template.
    override val staticServeDirs: Map<String, String> = mapOf(
            // This will serve the templateWeb directory in resources to /web/template
            "template" to javaClass.classLoader.getResource("templateWeb").toExternalForm()
    )
}
