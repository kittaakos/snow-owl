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
package com.b2international.snowowl.snomed.datastore.internal.id;

/**
 * Bean to represent a SNOMED CT id used by IHTSDO's ID generation service.
 * Instances of this class are meant to be serialized and deserialized
 * to and from Json.
 */
public class SctId {
	
	private String sctid;
	
	private int sequence;
	
	private int namespace;
	
	private String partitionId;
	
	private int checkDigit;
	
	private String systemId;
	
	private String status;
	
	private String author;
	
	private String software;
	
	private String expirationDate;
	
	private String comment;
	
	private String jobId;
	
	private String created_at;
	
	private String modified_at;

	/**
	 * @return the sctid
	 */
	public String getSctid() {
		return sctid;
	}

	/**
	 * @param sctid the sctid to set
	 */
	public void setSctid(String sctid) {
		this.sctid = sctid;
	}

	/**
	 * @return the sequence
	 */
	public int getSequence() {
		return sequence;
	}

	/**
	 * @param sequence the sequence to set
	 */
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	/**
	 * @return the namespace
	 */
	public int getNamespace() {
		return namespace;
	}

	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(int namespace) {
		this.namespace = namespace;
	}

	/**
	 * @return the partitionId
	 */
	public String getPartitionId() {
		return partitionId;
	}

	/**
	 * @param partitionId the partitionId to set
	 */
	public void setPartitionId(String partitionId) {
		this.partitionId = partitionId;
	}

	/**
	 * @return the checkDigit
	 */
	public int getCheckDigit() {
		return checkDigit;
	}

	/**
	 * @param checkDigit the checkDigit to set
	 */
	public void setCheckDigit(int checkDigit) {
		this.checkDigit = checkDigit;
	}

	/**
	 * @return the systemId
	 */
	public String getSystemId() {
		return systemId;
	}

	/**
	 * @param systemId the systemId to set
	 */
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the software
	 */
	public String getSoftware() {
		return software;
	}

	/**
	 * @param software the software to set
	 */
	public void setSoftware(String software) {
		this.software = software;
	}

	/**
	 * @return the expirationDate
	 */
	public String getExpirationDate() {
		return expirationDate;
	}

	/**
	 * @param expirationDate the expirationDate to set
	 */
	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the jobId
	 */
	public String getJobId() {
		return jobId;
	}

	/**
	 * @param jobId the jobId to set
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * @return the created_at
	 */
	public String getCreated_at() {
		return created_at;
	}

	/**
	 * @param created_at the created_at to set
	 */
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	/**
	 * @return the modified_at
	 */
	public String getModified_at() {
		return modified_at;
	}

	/**
	 * @param modified_at the modified_at to set
	 */
	public void setModified_at(String modified_at) {
		this.modified_at = modified_at;
	}

}
