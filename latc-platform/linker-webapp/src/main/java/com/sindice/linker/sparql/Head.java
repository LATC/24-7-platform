package com.sindice.linker.sparql;

import java.io.Serializable;
import java.util.List;

public class Head implements Serializable{
	
	private List<String> vars;

	public List<String> getVars() {
		return vars;
	}

	public void setVars(List<String> vars) {
		this.vars = vars;
	}

}
