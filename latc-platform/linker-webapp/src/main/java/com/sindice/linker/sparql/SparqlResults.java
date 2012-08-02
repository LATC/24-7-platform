package com.sindice.linker.sparql;

import java.io.Serializable;


public class SparqlResults implements Serializable{

	private Head head;
	private Results results;
	
	public Head getHead() {
		return head;
	}
	public void setHead(Head head) {
		this.head = head;
	}
	public Results getResults() {
		return results;
	}
	public void setResults(Results results) {
		this.results = results;
	}
}
