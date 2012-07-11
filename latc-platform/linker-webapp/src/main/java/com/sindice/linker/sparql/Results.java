package com.sindice.linker.sparql;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Results implements Serializable{

	private List<Map<String,Binding>> bindings;

	public List<Map<String, Binding>> getBindings() {
		return bindings;
	}

	public void setBindings(List<Map<String, Binding>> bindings) {
		this.bindings = bindings;
	}
}
