package com.b2international.snowowl.core.store;

import java.util.Objects;

import com.b2international.snowowl.core.store.index.Mapping;
import com.b2international.snowowl.core.terminology.Component;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @since 4.1
 */
class Types {

	@Mapping(type = "data")
	static class Data extends Component {
		
		@JsonProperty
		public String prop;
		
		@JsonCreator
		public Data(@JsonProperty("id") String id, @JsonProperty("prop") String prop) {
			setId(id);
			this.prop = prop;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(getId(), prop);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Data)) return false;
			final Data other = (Data) obj;
			return Objects.equals(getId(), other.getId()) && Objects.equals(prop, other.prop);
		}
		
	}
	
	static class ComplexData {
		
		@JsonProperty
		private String id;
		
		@JsonProperty
		private String name;
		
		@JsonProperty
		private State state;

		@JsonCreator
		public ComplexData(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("state") State state) {
			this.id = id;
			this.name = name;
			this.state = state;
		}
		
		public String getId() {
			return id;
		}
		
		@Override
		public int hashCode() {
			return Objects.hashCode(id);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof ComplexData)) return false;
			return Objects.equals(id, id);
		}
		
	}
	
	static enum State {
		SCHEDULED, RUNNING, FAILED
	}
	
}
