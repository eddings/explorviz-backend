package net.explorviz.resources

import javax.inject.Inject
import javax.ws.rs.core.MediaType
import javax.ws.rs.Produces
import javax.ws.rs.GET
import javax.ws.rs.Path
import net.explorviz.server.repository.LandscapeRepositoryModel
import javax.ws.rs.PathParam
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import net.explorviz.server.security.Secured

@Path("landscapes")
class LandscapeResource {

	var LandscapeRepositoryModel service

	@Inject
	def LandscapeResource(LandscapeRepositoryModel service) {
		this.service = service
	}

	@Secured
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("/{landscapeId}")
	def ObjectNode getLandscape(@PathParam("landscapeId") String landscapeId) {

		// harcoded
		var JsonNodeFactory factory = JsonNodeFactory.instance

		var response = factory.objectNode

		var dataObject = factory.objectNode

		response.set("data", dataObject)

		dataObject.put("id", "1")
		dataObject.put("type", "landscape")

		var landscapeAttributes = factory.objectNode
		landscapeAttributes.put("hash", 124572345)
		landscapeAttributes.put("activities", 34234234)

		dataObject.set("attributes", landscapeAttributes)

		var relationships = factory.objectNode

		var relationshipsSystemData = factory.objectNode
		relationshipsSystemData.put("id", "2")
		relationshipsSystemData.put("type", "system")

		var relationshipsData = factory.objectNode
		relationshipsData.set("data", relationshipsSystemData)

		relationships.set("systems", relationshipsData)

		dataObject.set("relationships", relationships)

		var included = factory.arrayNode

		response.set("included", included)

		var system = factory.objectNode

		system.put("id", "2")
		system.put("type", "system")

		var systemAttributes = factory.objectNode
		systemAttributes.put("opened", false)
		systemAttributes.put("name", "<UNKNOWN-APPLICATION>")

		system.set("attributes", systemAttributes)

		included.add(system)

		response

//		{
//  "data": {
//    "id": "1",
//    "type": "landscape",
//    "attributes": {
//      "hash": 124572345,
//      "activities": 34234234
//    },
//    "relationships": {
//      "systems": {
//        "data": {
//          "id": "2",
//          "type": "system"
//        }
//      }
//    }
//  },
//  "included": [
//    {
//      "id": "2",
//      "type": "system",
//      "attributes": {
//        "opened": false,
//        "name": "<UNKNOWN-APPLICATION>"
//      }
//    }
//  ]
//}
	}
}
