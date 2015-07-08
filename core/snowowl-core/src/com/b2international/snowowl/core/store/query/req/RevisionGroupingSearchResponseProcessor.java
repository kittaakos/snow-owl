package com.b2international.snowowl.core.store.query.req;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import com.b2international.snowowl.core.store.index.tx.Revision;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Response processor groups {@link SearchHit}s by a revision storageKey and returns only the most recent revision of a concept.
 * Note: To use this processor, queries should define commitTimestamp DESC sorting.
 * 
 * @since 5.0
 */
public class RevisionGroupingSearchResponseProcessor implements SearchResponseProcessor {

	private ObjectMapper mapper;

	public RevisionGroupingSearchResponseProcessor(ObjectMapper mapper) {
		this.mapper = checkNotNull(mapper, "Mapper may not be null");
	}

	@Override
	public <T> Iterable<T> process(SearchResponse response, Class<T> resultType) {
		final Map<Long, T> latestRevisions = newHashMap();
		final Collection<Long> deletedStorageKeys = newHashSet();
		for (SearchHit hit : response.getHits()) {
			final Map<String, Object> source = hit.getSource();
			final Long storageKey = (Long) source.get(Revision.STORAGE_KEY);
			if (!latestRevisions.containsKey(storageKey) && !deletedStorageKeys.contains(storageKey)) {
				// if the revision is a deleted one then add it to the deleted ones, so older revisions will be completely skipped
				final boolean deleted = (boolean) source.get(Revision.DELETED);
				if (deleted) {
					deletedStorageKeys.add(storageKey);
				} else {
					// if not deleted, and the latest revision, add it to the result
					latestRevisions.put(storageKey, mapper.convertValue(source, resultType));
				}
			}
			
		}
		return latestRevisions.values();
	}

}
