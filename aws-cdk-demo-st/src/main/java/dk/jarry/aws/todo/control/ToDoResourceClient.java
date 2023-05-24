package dk.jarry.aws.todo.control;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("todos")
@RegisterRestClient(configKey = "extensions-api")
public interface ToDoResourceClient {

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	JsonObject create(JsonObject toDo);

	@GET
	@Path("{uuid}")
	JsonObject read(@PathParam("uuid") String uuid);

	@PUT
	@Path("{uuid}")
	JsonObject update(@PathParam("uuid") String uuid, JsonObject toDo);

	@DELETE
	@Path("{uuid}")
	public void delete(@PathParam("uuid") String uuid);

	@GET
	public JsonArray list( //
			@QueryParam("from") Integer from, //
			@QueryParam("limit") Integer limit);
}
