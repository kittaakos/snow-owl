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
package com.b2international.snowowl.core.store.index;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;

import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

/**
 * Query builder to simplify building elasticsearch based index queries.
 * 
 * @since 5.0
 */
public class IndexQueryBuilder {

	private String type;
	private QueryBuilder where;
	private FilterBuilder filter;
	private Collection<SortBuilder> sorts = newHashSet();
	private Index index;
	private int offset = 0;
	private int limit = Integer.MAX_VALUE; // TODO is it okay to use max val here by default??? should we configure this???
	
	protected IndexQueryBuilder(Index index, String type) {
		this.index = checkNotNull(index, "index");
		this.type = checkNotNull(type, "Type may not be null");
	}
	
	public final IndexQueryBuilder where(QueryBuilder where) {
		this.where = where;
		return this;
	}
	
	public final IndexQueryBuilder filter(FilterBuilder filter) {
		this.filter = filter;
		return this;
	}
	
	public final IndexQueryBuilder sortAsc(String field) {
		this.sorts.add(SortBuilders.fieldSort(field).order(SortOrder.ASC));
		return this;
	}
	
	public final IndexQueryBuilder sortDesc(String field) {
		this.sorts.add(SortBuilders.fieldSort(field).order(SortOrder.DESC));
		return this;
	}
	
	public final IndexQueryBuilder page(int offset, int limit) {
		this.offset = offset;
		this.limit = limit;
		return this;
	}
	
	public SearchHits search() {
		return index.search(this);
	}
	
	protected QueryBuilder toIndexQuery() {
		if (filter != null) {
			return QueryBuilders.filteredQuery(where, filter);
		} else {
			return where;
		}
	}
	
	/*package*/ String type() {
		return type;
	}

	/*package*/ int offset() {
		return offset;
	}
	
	/*package*/ int limit() {
		return limit;
	}
	
	/*package*/ Collection<SortBuilder> sorts() {
		return sorts;
	}

}
