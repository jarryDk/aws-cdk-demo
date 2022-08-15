package dk.jarry.todo.entity;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import dk.jarry.todo.boundary.AbstractService;
import io.quarkus.runtime.annotations.RegisterForReflection;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@RegisterForReflection
public class ToDo {

	@Schema(readOnly = true)
	public String uuid;
	public String subject;
	public String body;
	@Schema(readOnly = true)
	public ZonedDateTime timestamp;

	public ToDo() {
		this.uuid = UUID.randomUUID().toString();
		this.timestamp = ZonedDateTime.now();
	}

	public static ToDo from(Map<String, AttributeValue> item) {
		ToDo toDo = new ToDo();
		if (item != null && !item.isEmpty()) {
			toDo.setUuid(item.get(AbstractService.TODO_UUID_COL).s());
			toDo.setSubject(item.get(AbstractService.TODO_SUBJECT_COL).s());
			toDo.setBody(item.get(AbstractService.TODO_BODY_COL).s());
			if(
				item.get(AbstractService.TODO_TIMESTAMP_COL).s() != null &&
				!item.get(AbstractService.TODO_TIMESTAMP_COL).s().isEmpty()){
				toDo.setTimestamp(ZonedDateTime.parse(item.get(AbstractService.TODO_TIMESTAMP_COL).s()));
			}
		}
		return toDo;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public ZonedDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(ZonedDateTime timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ToDo other = (ToDo) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

}
