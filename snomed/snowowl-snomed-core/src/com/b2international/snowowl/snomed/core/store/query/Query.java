/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.b2international.snowowl.snomed.core.store.query;


/**
 * Represents a SNOMED CT query.
 * 
 * @since 5.0
 */
public class Query {
	
	public interface EmptyBuilder {
		AfterSelectBuilder select(Select select);
	}
	
	public interface AfterTypeBuilder {
		AfterWhereBuilder where(Expression expression);
	}
	
	public interface AfterSelectBuilder {
		AfterTypeBuilder from(Type type);
	}
	
	public interface AfterWhereBuilder extends Buildable<Query> {
		AfterWhereBuilder offset(int offset);
		AfterWhereBuilder limit(int limit);
		AfterWhereBuilder sortBy(SortBy sortBy);
	}
	
	private static final class BuilderImpl implements EmptyBuilder, AfterTypeBuilder, AfterSelectBuilder, AfterWhereBuilder {
		
		private static final int DEFAULT_LIMIT = Integer.MAX_VALUE;
		
		private int offset = 0;
		private int limit = DEFAULT_LIMIT;
		private Type type;
		private Select select;
		private Expression where;
		private SortBy sortBy = SortBy.NONE;

		public BuilderImpl offset(int offset) {
			this.offset = offset;
			return this;
		}
		
		public BuilderImpl limit(int limit) {
			this.limit = limit;
			return this;
		}
		
		public BuilderImpl select(Select select) {
			this.select = select;
			return this;
		}
		
		public BuilderImpl where(Expression expression) {
			this.where = expression;
			return this;
		}
		
		public BuilderImpl sortBy(SortBy sortBy) {
			this.sortBy = sortBy;
			return this;
		}
		
		public BuilderImpl from(Type type) {
			this.type = type;
			return this;
		}
		
		public Query build() {
			Query query = new Query();
			query.setSelect(select);
			query.setType(type);
			query.setWhere(where);
			query.setLimit(limit);
			query.setOffset(offset);
			query.setSortBy(sortBy);
			return query;
		}
	}

	private int offset;
	private int limit;
	private Type type;
	private Select select;
	private Expression where;
	private SortBy sortBy;
	
	Query() {}
	
	public int getOffset() {
		return offset;
	}

	void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLimit() {
		return limit;
	}

	void setLimit(int limit) {
		this.limit = limit;
	}

	public Select getSelect() {
		return select;
	}

	void setSelect(Select select) {
		this.select = select;
	}

	public Expression getWhere() {
		return where;
	}

	void setWhere(Expression where) {
		this.where = where;
	}

	public SortBy getSortBy() {
		return sortBy;
	}

	void setSortBy(SortBy sortBy) {
		this.sortBy = sortBy;
	}

	public Type getType() {
		return type;
	}
	
	void setType(Type type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		// TODO: add sort, offset, limit
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT " + select + " FROM " + type + " WHERE " + where);
		if (sortBy != SortBy.NONE) {
			sb.append(" SORT BY " + sortBy);
		}
		sb.append(" LIMIT " + limit);
		if (offset != 0) {
			sb.append(" OFFSET " + offset);
		}
		return sb.toString();
	}
	
	public static EmptyBuilder builder() {
		return new BuilderImpl();
	}
}
