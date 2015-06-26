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

	@Mapping(type = "typewoid")
	static class TypeWithoutIdAnnotation {
		
		private String id;
		
		public TypeWithoutIdAnnotation(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
		
	}
	
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
	
	static class TypeWithIdMethod {
		
		private String id;
		
		public TypeWithIdMethod(String id) {
			this.id = id;
		}
		
		@Id
		public String getId() {
			return id;
		}
		
	}
	
	static interface TypeWithIdInterface {
		@Id
		String getId();
	}
	
	static class TypeWithIdInterfaceSubclass implements TypeWithIdInterface {

		private String id;

		public TypeWithIdInterfaceSubclass(String id) {
			this.id = id;
		}
		
		@Override
		public String getId() {
			return id;
		}
		
	}
	
}
