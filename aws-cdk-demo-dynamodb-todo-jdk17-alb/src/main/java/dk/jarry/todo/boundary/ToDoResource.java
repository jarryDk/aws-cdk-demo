package dk.jarry.todo.boundary;

import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

import dk.jarry.todo.entity.ToDo;

@Path("/todos")
public class ToDoResource {

	@Inject
	ToDoService toDoService;
	
	@Inject
    @ConfigProperty(defaultValue = "hello, quarkus on localhost", name="message")
    String message;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String hello() {
		return message;
	}

	@POST
	@Operation(description = "Create a new todo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response create(ToDo toDo) {
		return Response.status(201) //
				.entity(toDoService.create(toDo)) //
				.build();
	}

	@GET
	@Path("{uuid}")
	@Operation(description = "Get a specific todo by uuid")
	public ToDo read(@PathParam("uuid") String uuid) {
		Optional<ToDo> read = toDoService.read(uuid);				
		return read.map(v -> {
			return v;
		}).orElseThrow(() -> new WebApplicationException( //
				"ToDo with uuid of " + uuid + " does not exist.", //
				Response.Status.NOT_FOUND));
	}

	@GET
	@Path("/tablename")
	@Operation(description = "Get tablename")
	public String getTableName() {
		return toDoService.getTableName();
	}

	@PUT
	@Path("{uuid}")
	@Operation(description = "Update an exiting todo")
	public ToDo update(@PathParam("uuid") String uuid, ToDo toDo) {
		Optional<ToDo> update = toDoService.update(uuid, toDo);
		return update.map(v -> {
			return v;
		}).orElseThrow(() -> new WebApplicationException( //
				"ToDo with uuid of " + uuid + " does not exist.", //
				Response.Status.NOT_FOUND));
	}

	@DELETE
	@Path("{uuid}")
	@Operation(description = "Delete a specific todo by uuid")
	public void delete(@PathParam("uuid") String uuid) {
		toDoService.delete(uuid);
	}

	@GET
	@Operation(description = "Get all the todos")
	public List<ToDo> findAll( //
			@DefaultValue("0") @QueryParam("from") Integer from, //
			@DefaultValue("100") @QueryParam("limit") Integer limit) {
		return toDoService.findAll();
	}
}