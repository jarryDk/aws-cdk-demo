package dk.jarry.aws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import dk.jarry.aws.todo.control.ToDoResourceClient;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ToDoResourceTest {

	Logger LOGGER = Logger.getLogger(ToDoResourceTest.class.getName());
	
    @Inject
    @RestClient
    ToDoResourceClient resourceClient;
    
    @Test
    public void create() {

        JsonObjectBuilder createObjectBuilder = Json.createObjectBuilder();
        createObjectBuilder.add("subject", "Subject - test");
        createObjectBuilder.add("body", "Body - test");
        JsonObject todoInput = createObjectBuilder.build();
        
        var todoOutput = this.resourceClient.create(todoInput);
        
        LOGGER.info("Create : " + todoOutput.toString());

        assertEquals(todoInput.getString("subject"), todoOutput.getString("subject"));
        assertEquals(todoInput.getString("body"), todoOutput.getString("body"));        
       
    }

    @Test
    public void read() {

        JsonObjectBuilder createObjectBuilder = Json.createObjectBuilder();
        createObjectBuilder.add("subject", "Subject - test");
        createObjectBuilder.add("body", "Body - test");
        
        JsonObject todoInput = createObjectBuilder.build();        
        var todoOutput = this.resourceClient.create(todoInput);

        LOGGER.info("Create : " + todoOutput.toString());
        
        assertEquals(todoInput.getString("subject"), todoOutput.getString("subject"));
        assertEquals(todoInput.getString("body"), todoOutput.getString("body"));

        LOGGER.info("Read [1] " + todoOutput);

        String uuid = todoOutput.getString("uuid");

        todoOutput = this.resourceClient.read(uuid);

        assertEquals(todoInput.getString("subject"), todoOutput.getString("subject"));
        assertEquals(todoInput.getString("body"), todoOutput.getString("body"));

        LOGGER.info("Read [2] " + todoOutput);
       
    }

    @Test
    public void update() {

        JsonObjectBuilder createObjectBuilder = Json.createObjectBuilder();
        createObjectBuilder.add("subject", "Subject - test");
        createObjectBuilder.add("body", "Body - test");
        
        JsonObject todoInput = createObjectBuilder.build();        
        var todoOutput = this.resourceClient.create(todoInput);

        assertEquals(todoInput.getString("subject"), todoOutput.getString("subject"));
        assertEquals(todoInput.getString("body"), todoOutput.getString("body"));

        LOGGER.info("Update [1] " + todoOutput);

        String uuid = todoOutput.getString("uuid");

        JsonObjectBuilder todoUpdateBuilder = Json.createObjectBuilder(todoOutput);
        todoUpdateBuilder.add("subject", "Updated subject");
        var todoUpdated = todoUpdateBuilder.build();
         
        todoOutput = this.resourceClient.update(uuid, todoUpdated);

        assertEquals(todoUpdated.getString("subject"), "Updated subject");
        assertEquals(todoInput.getString("body"), todoOutput.getString("body"));

        LOGGER.info("Update[2] " + todoOutput);
       
    }

    @Test
    public void delete() {

        JsonObjectBuilder createObjectBuilder = Json.createObjectBuilder();
        createObjectBuilder.add("subject", "Subject - test");
        createObjectBuilder.add("body", "Body - test");
        
        JsonObject todoInput = createObjectBuilder.build();        
        var todoOutput = this.resourceClient.create(todoInput);

        assertEquals(todoInput.getString("subject"), todoOutput.getString("subject"));
        assertEquals(todoInput.getString("body"), todoOutput.getString("body"));

        LOGGER.info("Delete " + todoOutput);

        String uuid = todoOutput.getString("uuid");
       
        this.resourceClient.delete(uuid);

        try{
            todoOutput = this.resourceClient.read(uuid);
        } catch (javax.ws.rs.WebApplicationException we){
            assertTrue(we.getResponse().getStatus() == 404);
        }
       
    }

}