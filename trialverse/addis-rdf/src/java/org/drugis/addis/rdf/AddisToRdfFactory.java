package org.drugis.addis.rdf;

import clojure.lang.Symbol;
import clojure.lang.Var;
import clojure.lang.RT;

public class AddisToRdfFactory {
	static {
		Var require = RT.var("clojure.core", "require");
		require.invoke(Symbol.intern("org.drugis.addis.rdf.core"));
	}

	private static Var factory = RT.var("org.drugis.addis.rdf.core", "reify-converter");

	public static AddisToRdf create() {
		return (AddisToRdf) factory.invoke();
	}
}
