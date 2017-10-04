package srl.paros.corda

import net.corda.core.messaging.CordaRPCOps
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